package com.mediscan.backend.exception;

/**
 * Thrown for authentication failures (bad credentials, invalid/expired token).
 * Maps to HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
