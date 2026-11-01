package com.mediscan.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Standard envelope for all error responses.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Builder.Default
    private final boolean success = false;

    private final String message;

    private final String errorCode;

    /**
     * Field-level validation errors, e.g. {"email": "Email is required"}. Only populated for 400s.
     */
    private final Map<String, String> details;

    @Builder.Default
    private final Instant timestamp = Instant.now();
}
