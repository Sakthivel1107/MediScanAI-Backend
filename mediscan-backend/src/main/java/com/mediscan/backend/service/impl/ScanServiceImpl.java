package com.mediscan.backend.service.impl;

import com.mediscan.backend.dto.common.PageResponse;
import com.mediscan.backend.dto.scan.ScanResponse;
import com.mediscan.backend.enums.ScanStatus;
import com.mediscan.backend.exception.AiServiceException;
import com.mediscan.backend.exception.OcrProcessingException;
import com.mediscan.backend.exception.ResourceNotFoundException;
import com.mediscan.backend.model.Scan;
import com.mediscan.backend.repository.ScanRepository;
import com.mediscan.backend.service.AiService;
import com.mediscan.backend.service.ImageService;
import com.mediscan.backend.service.OcrService;
import com.mediscan.backend.service.ScanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the end-to-end medicine scan pipeline: image storage -> OCR -> AI
 * identification -> persistence. Deliberately delegates to {@link OcrService} and
 * {@link AiService} rather than talking to Tesseract/the LLM directly, so either
 * can be swapped out independently.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScanServiceImpl implements ScanService {

    private final ScanRepository scanRepository;
    private final ImageService imageService;
    private final OcrService ocrService;
    private final AiService aiService;

    @Override
    public ScanResponse processScan(String userId, MultipartFile file, String note) {
        String relativeImagePath = imageService.storeImage(file);

        Scan scan = Scan.builder()
                .userId(userId)
                .imageBase64("http://10.246.144.58:8080/uploads/"+relativeImagePath)
                .status(ScanStatus.PROCESSING)
                .build();
        scan = scanRepository.save(scan);

        try {
            File imageFile = imageService.resolveImageFile(relativeImagePath);
            OcrServiceImpl.OcrResult ocrResult = ocrService.extractText(imageFile);

            scan.setExtractedText(ocrResult.getExtractedText());

            AiServiceImpl.MedicineExtractionResult aiResult = aiService.identifyMedicine(ocrResult.getExtractedText());

            scan.setUses(orUnknown(aiResult.getUses()));
            scan.setAlternativeMedicines(Collections.singletonList(orUnknown(String.valueOf(aiResult.getAlternativeMedicines()))));
            scan.setMedicineName(orUnknown(aiResult.getMedicineName()));
            scan.setGenericName(orUnknown(aiResult.getGenericName()));
            scan.setPurpose(orUnknown(aiResult.getPurpose()));
            scan.setDosage(orUnknown(aiResult.getDosage()));
            scan.setSideEffects(orUnknown(aiResult.getSideEffects()));
            scan.setStorageInstructions(orUnknown(aiResult.getStorageInstructions()));
            scan.setPrescriptionRequired(Boolean.parseBoolean(orUnknown(String.valueOf(aiResult.isPrescriptionRequired()))));
            scan.setExpiryDate(aiResult.getExpiryDate());
            scan.setWarnings(aiResult.getWarnings() == null ? List.of() : aiResult.getWarnings());
            scan.setManufacturer(orUnknown(aiResult.getManufacturer()));
            scan.setBatchNumber(aiResult.getBatchNumber());
            scan.setStrength(aiResult.getConfidence() == null ? ocrResult.getConfidence() : aiResult.getConfidence());
            scan.setStatus(ScanStatus.COMPLETED);
            scan.setScannedAt(Instant.parse(orUnknown(String.valueOf(aiResult.getScannedAt()))));
            scan.setDisclaimer(orUnknown(aiResult.getDisclaimer()));
            scan.setSummary(orUnknown(aiResult.getSummary()));

        } catch (OcrProcessingException | AiServiceException e) {
            log.warn("Scan {} failed during processing: {}", scan.getId(), e.getMessage());
            scan.setStatus(ScanStatus.FAILED);
            scan.setFailureReason(e.getMessage());
            scanRepository.save(scan);
            throw e;
        }

        Scan saved = scanRepository.save(scan);
        log.info("Scan {} completed for user {}", saved.getId(), userId);
        return toResponse(saved);
    }

    @Override
    public PageResponse<ScanResponse> getScanHistory(String userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Scan> scans = scanRepository.findByUserIdOrderByScannedAtDesc(userId, pageRequest);

        List<ScanResponse> items = scans.getContent().stream().map(this::toResponse).toList();

        return PageResponse.<ScanResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalItems(scans.getTotalElements())
                .totalPages(scans.getTotalPages())
                .build();
    }

    @Override
    public ScanResponse getScanById(String userId, String scanId) {
        Scan scan = findOwnedScan(userId, scanId);
        return toResponse(scan);
    }

    @Override
    public void deleteScan(String userId, String scanId) {
        findOwnedScan(userId, scanId); // ensures ownership + existence
        scanRepository.deleteByIdAndUserId(scanId, userId);
        log.info("Scan {} deleted by user {}", scanId, userId);
    }

    private Scan findOwnedScan(String userId, String scanId) {
        Optional<Scan> scan = scanRepository.findByIdAndUserId(scanId, userId);
        return scan.orElseThrow(() -> new ResourceNotFoundException("Scan not found"));
    }

    private String orUnknown(String value) {
        return (value == null || value.isBlank()) ? "Unknown" : value;
    }

    private ScanResponse toResponse(Scan scan) {
        return ScanResponse.builder()
                .id(scan.getId())
                .medicineName(scan.getMedicineName())
                .purpose(scan.getPurpose())
                .dosage(scan.getDosage())
                .expiryDate(scan.getExpiryDate())
                .warnings(String.valueOf(scan.getWarnings()))
                .manufacturer(scan.getManufacturer())
                .batchNumber(scan.getBatchNumber())
                .scannedAt(scan.getScannedAt())
                .strength(String.valueOf(scan.getStrength()))
                .uses(scan.getUses())
                .sideEffects(scan.getSideEffects())
                .storageInstructions(scan.getStorageInstructions())
                .prescriptionRequired(scan.isPrescriptionRequired())
                .alternativeMedicines(scan.getAlternativeMedicines())
                .summary(scan.getSummary())
                .imageBase64(scan.getImageBase64())
                .disclaimer(scan.getDisclaimer())
                .build();
    }
}
