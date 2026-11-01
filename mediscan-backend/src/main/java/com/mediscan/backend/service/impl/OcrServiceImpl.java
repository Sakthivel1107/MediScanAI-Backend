package com.mediscan.backend.service.impl;

import com.mediscan.backend.exception.OcrProcessingException;
import com.mediscan.backend.service.OcrService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
public class OcrServiceImpl implements OcrService {

    private final ITesseract tesseract;

    /*
     * Important medicine information.
     *
     * These keywords are used to move important OCR lines
     * to the beginning of the final OCR result.
     */
    private static final Pattern IMPORTANT_MEDICINE_TEXT =
            Pattern.compile(
                    "(?i).*\\b(" +
                            "EXP|EXP\\.|EXPIRY|EXPIRY\\.?DATE|" +
                            "EXP\\.?DATE|USE.?BEFORE|VALID.?UPTO|VALID.?UNTIL|" +
                            "MFG|MFG\\.|MFD|MFD\\.|MANUFACTURED|" +
                            "BATCH|BATCH\\.?NO|B\\.NO|B\\.NO\\.|LOT|LOT\\.?NO|" +
                            "MRP|M\\.R\\.P|" +
                            "MG|MCG|ML|" +
                            "TABLET|TABLETS|CAPSULE|CAPSULES|SYRUP|" +
                            "PHARMA|PHARMACEUTICAL|PHARMACEUTICALS|" +
                            "LTD|LIMITED|PVT" +
                            ")\\b.*"
            );

    /*
     * Date-like pattern.
     *
     * Examples:
     *
     * JUN-2018
     * JUL-2016
     * 06/2018
     * 06-2018
     * 06/18
     * 2018-06
     */
    private static final Pattern DATE_PATTERN =
            Pattern.compile(
                    "(?i).*(" +
                            "\\b(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)[A-Z]*[- /.]20\\d{2}\\b" +
                            "|" +
                            "\\b(?:0?[1-9]|1[0-2])[-/.]20\\d{2}\\b" +
                            "|" +
                            "\\b(?:0?[1-9]|1[0-2])[-/.]\\d{2}\\b" +
                            "|" +
                            "\\b20\\d{2}[-/.](?:0?[1-9]|1[0-2])\\b" +
                            ").*"
            );

    public OcrServiceImpl(
            @Value("${tesseract.path}") String tesseractPath,
            @Value("${tesseract.language}") String language) {

        this.tesseract = new Tesseract();

        /*
         * Example:
         *
         * tesseract.path=C:/Program Files/Tesseract-OCR
         *
         * becomes:
         *
         * C:/Program Files/Tesseract-OCR/tessdata
         */
        String tessdataPath =
                tesseractPath + File.separator + "tessdata";

        if (StringUtils.hasText(tessdataPath)) {
            this.tesseract.setDatapath(tessdataPath);
        }

        this.tesseract.setLanguage(language);

        /*
         * Preserve spaces between OCR words.
         *
         * Useful for:
         *
         * EXP. DATE: JUN-2018
         * BATCH NO: BEYBN2116
         */
        try {

            this.tesseract.setVariable(
                    "preserve_interword_spaces",
                    "1"
            );

            /*
             * Tell Tesseract that our input is high resolution.
             */
            this.tesseract.setVariable(
                    "user_defined_dpi",
                    "300"
            );

        } catch (Exception e) {

            log.warn(
                    "Could not configure optional Tesseract variables",
                    e
            );
        }
    }

    @Override
    public OcrResult extractText(File imageFile) {

        if (imageFile == null) {

            throw new OcrProcessingException(
                    "Image file is null."
            );
        }

        if (!imageFile.exists()) {

            throw new OcrProcessingException(
                    "Image file does not exist."
            );
        }

        if (!imageFile.isFile()) {

            throw new OcrProcessingException(
                    "Uploaded path is not a valid file."
            );
        }

        log.info(
                "================================================="
        );

        log.info(
                "Starting enhanced medicine OCR"
        );

        log.info(
                "Image: {}",
                imageFile.getAbsolutePath()
        );

        log.info(
                "================================================="
        );

        try {

            // =========================================================
            // 1. READ ORIGINAL IMAGE
            // =========================================================

            BufferedImage original =
                    ImageIO.read(imageFile);

            if (original == null) {

                throw new OcrProcessingException(
                        "Unsupported or corrupted image."
                );
            }

            log.info(
                    "Original image: {} x {}",
                    original.getWidth(),
                    original.getHeight()
            );

            // =========================================================
            // 2. UPSCALE
            // =========================================================

            /*
             * Small text such as:
             *
             * EXP.DATE: JUN-2018
             *
             * benefits significantly from upscaling.
             */
            BufferedImage upscaled =
                    upscale(
                            original,
                            3
                    );

            log.info(
                    "Upscaled image: {} x {}",
                    upscaled.getWidth(),
                    upscaled.getHeight()
            );

            // =========================================================
            // 3. PREPROCESSING
            // =========================================================

            BufferedImage grayscale =
                    createGrayscale(
                            upscaled
                    );

            BufferedImage enhanced =
                    enhanceContrast(
                            grayscale
                    );

            BufferedImage sharpened =
                    sharpen(
                            enhanced
                    );

            BufferedImage threshold =
                    adaptiveThreshold(
                            enhanced
                    );

            // =========================================================
            // 4. OCR RESULTS
            // =========================================================

            List<String> results =
                    new ArrayList<>();

            // ---------------------------------------------------------
            // NORMAL FULL IMAGE OCR
            // ---------------------------------------------------------

            results.add(
                    runOCR(
                            enhanced,
                            6,
                            "FULL-ENHANCED-PSM6"
                    )
            );

            results.add(
                    runOCR(
                            enhanced,
                            11,
                            "FULL-ENHANCED-PSM11"
                    )
            );

            // ---------------------------------------------------------
            // SHARPENED
            // ---------------------------------------------------------

            results.add(
                    runOCR(
                            sharpened,
                            6,
                            "FULL-SHARPENED-PSM6"
                    )
            );

            results.add(
                    runOCR(
                            sharpened,
                            11,
                            "FULL-SHARPENED-PSM11"
                    )
            );

            // ---------------------------------------------------------
            // THRESHOLD
            // ---------------------------------------------------------

            results.add(
                    runOCR(
                            threshold,
                            6,
                            "FULL-THRESHOLD-PSM6"
                    )
            );

            results.add(
                    runOCR(
                            threshold,
                            11,
                            "FULL-THRESHOLD-PSM11"
                    )
            );

            // =========================================================
            // 5. ROTATED FULL IMAGE OCR
            // =========================================================

            /*
             * VERY IMPORTANT FOR YOUR IMAGE.
             *
             * The BATCH / MFG / EXP block is vertically printed.
             *
             * Therefore we OCR the complete image after rotating
             * it in both directions.
             */

            BufferedImage enhanced90 =
                    rotateImage(
                            enhanced,
                            90
                    );

            BufferedImage enhanced270 =
                    rotateImage(
                            enhanced,
                            270
                    );

            BufferedImage threshold90 =
                    rotateImage(
                            threshold,
                            90
                    );

            BufferedImage threshold270 =
                    rotateImage(
                            threshold,
                            270
                    );

            results.add(
                    runOCR(
                            enhanced90,
                            11,
                            "FULL-ROTATED-90"
                    )
            );

            results.add(
                    runOCR(
                            enhanced270,
                            11,
                            "FULL-ROTATED-270"
                    )
            );

            results.add(
                    runOCR(
                            threshold90,
                            11,
                            "FULL-THRESHOLD-ROTATED-90"
                    )
            );

            results.add(
                    runOCR(
                            threshold270,
                            11,
                            "FULL-THRESHOLD-ROTATED-270"
                    )
            );

            // =========================================================
            // 6. RIGHT SIDE CROP
            // =========================================================

            /*
             * In your uploaded image, the vertical:
             *
             * BATCH NO
             * MFG DATE
             * EXP DATE
             * M.R.P
             *
             * block is on the RIGHT side.
             *
             * We therefore create a dedicated right-side crop.
             */

            BufferedImage rightCrop =
                    cropRightSide(
                            enhanced
                    );

            BufferedImage rightCropThreshold =
                    cropRightSide(
                            threshold
                    );

            if (rightCrop != null) {

                log.info(
                        "Running dedicated right-side medicine information OCR"
                );

                // -----------------------------------------------------
                // RIGHT CROP NORMAL
                // -----------------------------------------------------

                results.add(
                        runOCR(
                                rightCrop,
                                6,
                                "RIGHT-CROP-PSM6"
                        )
                );

                results.add(
                        runOCR(
                                rightCrop,
                                11,
                                "RIGHT-CROP-PSM11"
                        )
                );

                // -----------------------------------------------------
                // RIGHT CROP ROTATED
                // -----------------------------------------------------

                BufferedImage right90 =
                        rotateImage(
                                rightCrop,
                                90
                        );

                BufferedImage right270 =
                        rotateImage(
                                rightCrop,
                                270
                        );

                results.add(
                        runOCR(
                                right90,
                                6,
                                "RIGHT-CROP-ROTATED-90-PSM6"
                        )
                );

                results.add(
                        runOCR(
                                right90,
                                11,
                                "RIGHT-CROP-ROTATED-90-PSM11"
                        )
                );

                results.add(
                        runOCR(
                                right270,
                                6,
                                "RIGHT-CROP-ROTATED-270-PSM6"
                        )
                );

                results.add(
                        runOCR(
                                right270,
                                11,
                                "RIGHT-CROP-ROTATED-270-PSM11"
                        )
                );
            }

            // =========================================================
            // 7. THRESHOLDED RIGHT CROP
            // =========================================================

            if (rightCropThreshold != null) {

                BufferedImage rightThreshold90 =
                        rotateImage(
                                rightCropThreshold,
                                90
                        );

                BufferedImage rightThreshold270 =
                        rotateImage(
                                rightCropThreshold,
                                270
                        );

                results.add(
                        runOCR(
                                rightCropThreshold,
                                11,
                                "RIGHT-THRESHOLD"
                        )
                );

                results.add(
                        runOCR(
                                rightThreshold90,
                                11,
                                "RIGHT-THRESHOLD-ROTATED-90"
                        )
                );

                results.add(
                        runOCR(
                                rightThreshold270,
                                11,
                                "RIGHT-THRESHOLD-ROTATED-270"
                        )
                );
            }

            // =========================================================
            // 8. COMBINE ALL OCR RESULTS
            // =========================================================

            String combinedText =
                    combineOCRResults(
                            results
                    );

            log.info(
                    "================================================="
            );

            log.info(
                    "COMBINED OCR TEXT:"
            );

            log.info(
                    "\n{}",
                    combinedText
            );

            log.info(
                    "================================================="
            );

            // =========================================================
            // 9. PRIORITIZE MEDICINE INFORMATION
            // =========================================================

            String importantText =
                    prioritizeImportantLines(
                            combinedText
                    );

            // =========================================================
            // 10. PRIORITIZE DATE LINES
            // =========================================================

            String dateText =
                    extractDateRelatedLines(
                            combinedText
                    );

            // =========================================================
            // 11. BUILD FINAL RESULT
            // =========================================================

            String finalText =
                    buildFinalOCRText(
                            importantText,
                            dateText,
                            combinedText
                    );

            // =========================================================
            // 12. CONFIDENCE
            // =========================================================

            double confidence =
                    estimateConfidence(
                            finalText
                    );

            log.info(
                    "Final OCR characters: {}",
                    finalText.length()
            );

            log.info(
                    "Estimated OCR confidence: {}",
                    confidence
            );

            return new OcrResult(
                    finalText.trim(),
                    confidence
            );

        } catch (IOException e) {

            log.error(
                    "Failed to read uploaded image",
                    e
            );

            throw new OcrProcessingException(
                    "Failed to process the uploaded image.",
                    e
            );

        } catch (TesseractException e) {

            log.error(
                    "Tesseract OCR failed",
                    e
            );

            throw new OcrProcessingException(
                    "Failed to extract text from the uploaded image.",
                    e
            );

        } catch (OcrProcessingException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Unexpected OCR error",
                    e
            );

            throw new OcrProcessingException(
                    "Unexpected error while processing the image.",
                    e
            );
        }
    }

    // =================================================================
    // TESSERACT
    // =================================================================

    private String runOCR(
            BufferedImage image,
            int pageSegmentationMode,
            String variant
    ) throws TesseractException {

        if (image == null) {
            return "";
        }

        try {

            /*
             * PSM 6:
             *
             * Assume a single uniform block of text.
             *
             * Good for normal medicine-strip text.
             *
             * PSM 11:
             *
             * Sparse text.
             *
             * Good for BATCH / MFG / EXP regions.
             */
            tesseract.setPageSegMode(
                    pageSegmentationMode
            );

            String text =
                    tesseract.doOCR(
                            image
                    );

            if (text == null) {
                text = "";
            }

            text =
                    cleanOCRText(
                            text
                    );

            log.info(
                    "OCR [{}] -> {} characters",
                    variant,
                    text.length()
            );

            /*
             * Log short/important OCR results.
             *
             * This is extremely useful while testing
             * your medicine image.
             */
            if (containsImportantInformation(text)) {

                log.info(
                        "IMPORTANT OCR [{}]:\n{}",
                        variant,
                        text
                );
            }

            return text;

        } catch (TesseractException e) {

            /*
             * One OCR variant failing should NOT make the
             * entire medicine scan fail.
             */
            log.warn(
                    "OCR variant [{}] failed: {}",
                    variant,
                    e.getMessage()
            );

            return "";
        }
    }

    // =================================================================
    // UPSCALE
    // =================================================================

    private BufferedImage upscale(
            BufferedImage source,
            int scale
    ) {

        int width =
                source.getWidth() * scale;

        int height =
                source.getHeight() * scale;

        BufferedImage result =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics =
                result.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        graphics.drawImage(
                source,
                0,
                0,
                width,
                height,
                null
        );

        graphics.dispose();

        return result;
    }

    // =================================================================
    // GRAYSCALE
    // =================================================================

    private BufferedImage createGrayscale(
            BufferedImage source
    ) {

        BufferedImage result =
                new BufferedImage(
                        source.getWidth(),
                        source.getHeight(),
                        BufferedImage.TYPE_BYTE_GRAY
                );

        Graphics2D graphics =
                result.createGraphics();

        graphics.drawImage(
                source,
                0,
                0,
                null
        );

        graphics.dispose();

        return result;
    }

    // =================================================================
    // CONTRAST
    // =================================================================

    private BufferedImage enhanceContrast(
            BufferedImage source
    ) {

        BufferedImage result =
                new BufferedImage(
                        source.getWidth(),
                        source.getHeight(),
                        BufferedImage.TYPE_BYTE_GRAY
                );

        int width =
                source.getWidth();

        int height =
                source.getHeight();

        int min = 255;
        int max = 0;

        /*
         * Find darkest and brightest pixels.
         */
        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int value =
                        source.getRaster()
                                .getSample(
                                        x,
                                        y,
                                        0
                                );

                if (value < min) {
                    min = value;
                }

                if (value > max) {
                    max = value;
                }
            }
        }

        /*
         * Nothing useful to enhance.
         */
        if (max <= min) {
            return source;
        }

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int value =
                        source.getRaster()
                                .getSample(
                                        x,
                                        y,
                                        0
                                );

                int enhanced =
                        (value - min) * 255
                                / (max - min);

                enhanced =
                        Math.max(
                                0,
                                Math.min(
                                        255,
                                        enhanced
                                )
                        );

                result.getRaster()
                        .setSample(
                                x,
                                y,
                                0,
                                enhanced
                        );
            }
        }

        return result;
    }

    // =================================================================
    // SHARPEN
    // =================================================================

    private BufferedImage sharpen(
            BufferedImage source
    ) {

        int width =
                source.getWidth();

        int height =
                source.getHeight();

        BufferedImage result =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_BYTE_GRAY
                );

        /*
         * Copy border pixels.
         */
        for (int x = 0; x < width; x++) {

            result.getRaster()
                    .setSample(
                            x,
                            0,
                            0,
                            source.getRaster()
                                    .getSample(
                                            x,
                                            0,
                                            0
                                    )
                    );

            result.getRaster()
                    .setSample(
                            x,
                            height - 1,
                            0,
                            source.getRaster()
                                    .getSample(
                                            x,
                                            height - 1,
                                            0
                                    )
                    );
        }

        for (int y = 0; y < height; y++) {

            result.getRaster()
                    .setSample(
                            0,
                            y,
                            0,
                            source.getRaster()
                                    .getSample(
                                            0,
                                            y,
                                            0
                                    )
                    );

            result.getRaster()
                    .setSample(
                            width - 1,
                            y,
                            0,
                            source.getRaster()
                                    .getSample(
                                            width - 1,
                                            y,
                                            0
                                    )
                    );
        }

        /*
         * Sharpening kernel:
         *
         *       -1
         *   -1   5   -1
         *       -1
         */
        for (int y = 1; y < height - 1; y++) {

            for (int x = 1; x < width - 1; x++) {

                int center =
                        source.getRaster()
                                .getSample(
                                        x,
                                        y,
                                        0
                                );

                int top =
                        source.getRaster()
                                .getSample(
                                        x,
                                        y - 1,
                                        0
                                );

                int bottom =
                        source.getRaster()
                                .getSample(
                                        x,
                                        y + 1,
                                        0
                                );

                int left =
                        source.getRaster()
                                .getSample(
                                        x - 1,
                                        y,
                                        0
                                );

                int right =
                        source.getRaster()
                                .getSample(
                                        x + 1,
                                        y,
                                        0
                                );

                int value =
                        (5 * center)
                                - top
                                - bottom
                                - left
                                - right;

                value =
                        Math.max(
                                0,
                                Math.min(
                                        255,
                                        value
                                )
                        );

                result.getRaster()
                        .setSample(
                                x,
                                y,
                                0,
                                value
                        );
            }
        }

        return result;
    }

    // =================================================================
    // ADAPTIVE THRESHOLD
    // =================================================================

    private BufferedImage adaptiveThreshold(
            BufferedImage source
    ) {

        int width =
                source.getWidth();

        int height =
                source.getHeight();

        BufferedImage result =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_BYTE_GRAY
                );

        int radius = 12;

        /*
         * To avoid excessive processing on extremely large images,
         * use a local average around every pixel.
         */
        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int startX =
                        Math.max(
                                0,
                                x - radius
                        );

                int endX =
                        Math.min(
                                width - 1,
                                x + radius
                        );

                int startY =
                        Math.max(
                                0,
                                y - radius
                        );

                int endY =
                        Math.min(
                                height - 1,
                                y + radius
                        );

                long sum = 0;

                int count = 0;

                for (int yy = startY;
                     yy <= endY;
                     yy++) {

                    for (int xx = startX;
                         xx <= endX;
                         xx++) {

                        sum +=
                                source.getRaster()
                                        .getSample(
                                                xx,
                                                yy,
                                                0
                                        );

                        count++;
                    }
                }

                int average =
                        (int) (
                                sum / count
                        );

                int pixel =
                        source.getRaster()
                                .getSample(
                                        x,
                                        y,
                                        0
                                );

                int threshold =
                        average - 10;

                int output =
                        pixel < threshold
                                ? 0
                                : 255;

                result.getRaster()
                        .setSample(
                                x,
                                y,
                                0,
                                output
                        );
            }
        }

        return result;
    }

    // =================================================================
    // ROTATE
    // =================================================================

    private BufferedImage rotateImage(
            BufferedImage source,
            int degrees
    ) {

        int width =
                source.getWidth();

        int height =
                source.getHeight();

        boolean swapDimensions =
                degrees == 90 ||
                        degrees == 270;

        int newWidth =
                swapDimensions
                        ? height
                        : width;

        int newHeight =
                swapDimensions
                        ? width
                        : height;

        BufferedImage rotated =
                new BufferedImage(
                        newWidth,
                        newHeight,
                        BufferedImage.TYPE_BYTE_GRAY
                );

        Graphics2D graphics =
                rotated.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        if (degrees == 90) {

            graphics.translate(
                    newWidth,
                    0
            );

            graphics.rotate(
                    Math.toRadians(90)
            );

        } else if (degrees == 180) {

            graphics.translate(
                    newWidth,
                    newHeight
            );

            graphics.rotate(
                    Math.toRadians(180)
            );

        } else if (degrees == 270) {

            graphics.translate(
                    0,
                    newHeight
            );

            graphics.rotate(
                    Math.toRadians(270)
            );
        }

        graphics.drawImage(
                source,
                0,
                0,
                null
        );

        graphics.dispose();

        return rotated;
    }

    // =================================================================
    // RIGHT SIDE CROP
    // =================================================================

    private BufferedImage cropRightSide(
            BufferedImage source
    ) {

        int width =
                source.getWidth();

        int height =
                source.getHeight();

        /*
         * Take approximately the rightmost 28%.
         *
         * Your sample image has the BATCH/MFG/EXP block
         * in this region.
         */
        int cropX =
                (int) (width * 0.70);

        int cropWidth =
                width - cropX;

        /*
         * Avoid an unusably small crop.
         */
        if (cropWidth < 100) {
            return null;
        }

        BufferedImage crop =
                new BufferedImage(
                        cropWidth,
                        height,
                        BufferedImage.TYPE_BYTE_GRAY
                );

        Graphics2D graphics =
                crop.createGraphics();

        graphics.drawImage(
                source,

                0,
                0,

                cropWidth,
                height,

                cropX,
                0,

                width,
                height,

                null
        );

        graphics.dispose();

        log.info(
                "Right-side crop: x={}, width={}, height={}",
                cropX,
                cropWidth,
                height
        );

        return crop;
    }

    // =================================================================
    // COMBINE OCR RESULTS
    // =================================================================

    private String combineOCRResults(
            List<String> results
    ) {

        Set<String> uniqueLines =
                new LinkedHashSet<>();

        for (String result : results) {

            if (!StringUtils.hasText(result)) {
                continue;
            }

            String[] lines =
                    result.split("\\R");

            for (String line : lines) {

                String normalized =
                        normalizeLine(
                                line
                        );

                if (normalized.length() < 2) {
                    continue;
                }

                /*
                 * Avoid garbage lines.
                 */
                if (isMostlyNoise(normalized)) {
                    continue;
                }

                uniqueLines.add(
                        normalized
                );
            }
        }

        StringBuilder builder =
                new StringBuilder();

        for (String line : uniqueLines) {

            builder
                    .append(line)
                    .append('\n');
        }

        return builder
                .toString()
                .trim();
    }

    // =================================================================
    // IMPORTANT LINES
    // =================================================================

    private String prioritizeImportantLines(
            String text
    ) {

        if (!StringUtils.hasText(text)) {
            return "";
        }

        List<String> important =
                new ArrayList<>();

        Set<String> added =
                new LinkedHashSet<>();

        String[] lines =
                text.split("\\R");

        for (String line : lines) {

            String normalized =
                    normalizeLine(
                            line
                    );

            if (normalized.isEmpty()) {
                continue;
            }

            if (IMPORTANT_MEDICINE_TEXT
                    .matcher(normalized)
                    .matches()) {

                String key =
                        normalized.toLowerCase();

                if (added.add(key)) {

                    important.add(
                            normalized
                    );
                }
            }
        }

        StringBuilder result =
                new StringBuilder();

        for (String line : important) {

            result
                    .append(line)
                    .append('\n');
        }

        return result
                .toString()
                .trim();
    }

    // =================================================================
    // DATE LINES
    // =================================================================

    private String extractDateRelatedLines(
            String text
    ) {

        if (!StringUtils.hasText(text)) {
            return "";
        }

        List<String> dateLines =
                new ArrayList<>();

        Set<String> added =
                new LinkedHashSet<>();

        String[] lines =
                text.split("\\R");

        for (String line : lines) {

            String normalized =
                    normalizeLine(
                            line
                    );

            if (normalized.isEmpty()) {
                continue;
            }

            boolean containsDate =
                    DATE_PATTERN
                            .matcher(normalized)
                            .matches();

            boolean containsExpiryKeyword =
                    normalized
                            .toUpperCase()
                            .contains("EXP");

            boolean containsMfgKeyword =
                    normalized
                            .toUpperCase()
                            .contains("MFG");

            if (containsDate ||
                    containsExpiryKeyword ||
                    containsMfgKeyword) {

                String key =
                        normalized.toLowerCase();

                if (added.add(key)) {

                    dateLines.add(
                            normalized
                    );
                }
            }
        }

        StringBuilder result =
                new StringBuilder();

        for (String line : dateLines) {

            result
                    .append(line)
                    .append('\n');
        }

        return result
                .toString()
                .trim();
    }

    // =================================================================
    // FINAL OCR TEXT
    // =================================================================

    private String buildFinalOCRText(
            String importantText,
            String dateText,
            String completeText
    ) {

        StringBuilder result =
                new StringBuilder();

        /*
         * Put important information FIRST.
         */
        if (StringUtils.hasText(importantText)) {

            result
                    .append(
                            "IMPORTANT MEDICINE INFORMATION:\n"
                    )
                    .append(
                            importantText
                    )
                    .append(
                            "\n\n"
                    );
        }

        /*
         * Put date-related lines separately.
         */
        if (StringUtils.hasText(dateText)) {

            result
                    .append(
                            "DATE INFORMATION:\n"
                    )
                    .append(
                            dateText
                    )
                    .append(
                            "\n\n"
                    );
        }

        /*
         * Complete OCR is retained because Gemini may
         * need surrounding context.
         */
        result
                .append(
                        "COMPLETE OCR TEXT:\n"
                )
                .append(
                        completeText
                );

        return result
                .toString()
                .trim();
    }

    // =================================================================
    // CLEAN OCR
    // =================================================================

    private String cleanOCRText(
            String text
    ) {

        if (!StringUtils.hasText(text)) {
            return "";
        }

        return text
                .replace('\r', '\n')
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    // =================================================================
    // NORMALIZE LINE
    // =================================================================

    private String normalizeLine(
            String line
    ) {

        if (line == null) {
            return "";
        }

        return line
                .replaceAll("\\s+", " ")
                .trim();
    }

    // =================================================================
    // NOISE DETECTION
    // =================================================================

    private boolean isMostlyNoise(
            String text
    ) {

        if (text.length() < 3) {
            return true;
        }

        int usefulCharacters = 0;

        for (int i = 0;
             i < text.length();
             i++) {

            char c =
                    text.charAt(i);

            if (Character.isLetterOrDigit(c)) {

                usefulCharacters++;
            }
        }

        /*
         * If less than roughly 25% of the characters are
         * letters/numbers, it is probably OCR garbage.
         */
        return usefulCharacters <
                Math.max(
                        2,
                        text.length() / 4
                );
    }

    // =================================================================
    // IMPORTANT INFORMATION DETECTION
    // =================================================================

    private boolean containsImportantInformation(
            String text
    ) {

        if (!StringUtils.hasText(text)) {
            return false;
        }

        return IMPORTANT_MEDICINE_TEXT
                .matcher(text)
                .matches();
    }

    // =================================================================
    // CONFIDENCE
    // =================================================================

    private double estimateConfidence(
            String text
    ) {

        if (!StringUtils.hasText(text)) {
            return 0.0;
        }

        int length =
                text.trim().length();

        int lines =
                text.split("\\R").length;

        double confidence;

        if (length < 10) {

            confidence = 0.25;

        } else if (length < 40) {

            confidence = 0.50;

        } else if (length < 100) {

            confidence = 0.70;

        } else {

            confidence = 0.85;
        }

        /*
         * Additional evidence that OCR found
         * structured medicine information.
         */
        String upper =
                text.toUpperCase();

        if (upper.contains("EXP")) {
            confidence += 0.05;
        }

        if (upper.contains("MFG")) {
            confidence += 0.03;
        }

        if (upper.contains("BATCH")) {
            confidence += 0.03;
        }

        if (lines >= 5) {
            confidence += 0.02;
        }

        return Math.min(
                confidence,
                0.98
        );
    }

    // =================================================================
    // RESULT CLASS
    // =================================================================

    @Getter
    public static class OcrResult {

        private final String extractedText;

        private final double confidence;

        public OcrResult(
                String extractedText,
                double confidence
        ) {

            this.extractedText =
                    extractedText;

            this.confidence =
                    confidence;
        }
    }
}