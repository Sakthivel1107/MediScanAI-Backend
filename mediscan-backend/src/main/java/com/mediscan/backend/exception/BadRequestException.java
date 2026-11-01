package com.mediscan.backend.exception;

/**
 * Thrown for malformed or semantically invalid requests (invalid file type,
 * duplicate email, malformed AI response, etc.). Maps to HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
