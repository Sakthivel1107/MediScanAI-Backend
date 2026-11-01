package com.mediscan.backend.model;

import com.mediscan.backend.enums.ScanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB document representing a single medicine strip scan performed by a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "scans")
public class Scan {

    @Id
    private String id;

    @Indexed
    private String userId;

    /**
     * Relative path (or URL) to the stored image on disk / object storage.
     */

    private String medicineName;
    private String genericName;
    private String purpose;
    private String uses;
    private String dosage;
    private String expiryDate;
    private String sideEffects;
    private String storageInstructions;
    private boolean prescriptionRequired;
    private List<String> alternativeMedicines;
    private List<String> warnings;
    private String manufacturer;
    private String batchNumber;
    private String imageBase64;
    private String disclaimer;
    private String summary;
    /**
     * Raw text extracted from the image via OCR.
     */
    private String extractedText;

    /**
     * Confidence score (0.0 - 1.0) reported by the AI/OCR pipeline.
     */
    private Double strength;

    @Builder.Default
    private ScanStatus status = ScanStatus.PENDING;

    /**
     * Reason for failure, populated only when status == FAILED.
     */
    private String failureReason;

    @Indexed
    @CreatedDate
    private Instant scannedAt;
}
