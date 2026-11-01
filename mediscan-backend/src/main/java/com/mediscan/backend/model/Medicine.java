package com.mediscan.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB document representing a known medicine catalog entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "medicines")
public class Medicine {

    @Id
    private String id;

    @Indexed
    private String name;

    private String genericName;
    private String purpose;
    private String dosage;
    private List<String> warnings;
    private List<String> sideEffects;
    private String manufacturer;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
