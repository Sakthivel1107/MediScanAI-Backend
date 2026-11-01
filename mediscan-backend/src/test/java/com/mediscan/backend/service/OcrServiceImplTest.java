package com.mediscan.backend.service;

import com.mediscan.backend.exception.OcrProcessingException;
import com.mediscan.backend.service.impl.OcrServiceImpl;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for {@link OcrServiceImpl}.
 *
 * <p>Note: Tess4J requires a native Tesseract installation on the host machine
 * (see TESSERACT_PATH in .env.example). This test only verifies that a missing/
 * unreadable image file is surfaced as our own {@link OcrProcessingException}
 * rather than leaking a raw TesseractException; it requires Tesseract to be
 * installed locally to run (see README "Tesseract installation/setup").
 */
class OcrServiceImplTest {

    @Test
    void extractText_shouldThrowOcrProcessingException_whenImageFileDoesNotExist() {
        OcrServiceImpl ocrService = new OcrServiceImpl("", "eng");
        File missingFile = new File("does-not-exist.jpg");

        assertThatThrownBy(() -> ocrService.extractText(missingFile))
                .isInstanceOf(OcrProcessingException.class);
    }
}
