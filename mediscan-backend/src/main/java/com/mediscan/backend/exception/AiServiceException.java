package com.mediscan.backend.exception;

/**
 * Thrown when the AI/LLM medicine-identification service fails or is unavailable.
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
