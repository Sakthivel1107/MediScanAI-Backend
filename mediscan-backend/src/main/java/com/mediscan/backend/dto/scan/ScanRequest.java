package com.mediscan.backend.dto.scan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Optional metadata accompanying a scan upload. The image itself arrives as a
 * multipart file part named "file"; this DTO carries any additional form fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanRequest {
    /**
     * Optional free-text note the user can attach to a scan (e.g. "found in travel bag").
     */
    private String note;
}
