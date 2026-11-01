package com.mediscan.backend.enums;

/**
 * Lifecycle status of a medicine scan as it moves through OCR and AI processing.
 */
public enum ScanStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}
