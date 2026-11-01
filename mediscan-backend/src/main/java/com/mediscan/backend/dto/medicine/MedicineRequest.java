package com.mediscan.backend.dto.medicine;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Payload for creating/updating a Medicine catalog entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineRequest {

    @NotBlank(message = "Medicine name is required")
    private String name;

    private String genericName;
    private String purpose;
    private String dosage;
    private List<String> warnings;
    private List<String> sideEffects;
    private String manufacturer;
}
