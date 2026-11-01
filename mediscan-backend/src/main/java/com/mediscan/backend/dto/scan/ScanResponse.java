package com.mediscan.backend.dto.scan;

import com.mediscan.backend.enums.ScanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Public-facing representation of a Scan, including a medical-information disclaimer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResponse {
    private String medicineName;
    private String manufacturer;
    private String batchNumber;
    private String expiryDate;
    private String strength;
    private String purpose;
    private String uses;
    private String dosage;
    private String sideEffects;
    private String warnings;
    private String storageInstructions;
    private boolean prescriptionRequired;
    private List<String> alternativeMedicines;
    private String summary;
    private String imageBase64;
    private Instant scannedAt;
    private String id;

    @Builder.Default
    private String disclaimer = "This information is extracted from the medicine package and provided "
            + "for informational purposes only. Verify medicine details with a pharmacist or healthcare professional.";
}
