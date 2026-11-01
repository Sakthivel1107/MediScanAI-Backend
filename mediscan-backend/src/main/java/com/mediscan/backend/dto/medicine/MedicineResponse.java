package com.mediscan.backend.dto.medicine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Public-facing representation of a Medicine catalog entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineResponse {
    private String id;
    private String name;
    private String genericName;
    private String purpose;
    private String dosage;
    private List<String> warnings;
    private List<String> sideEffects;
    private String manufacturer;
    private Instant createdAt;
    private Instant updatedAt;
}
