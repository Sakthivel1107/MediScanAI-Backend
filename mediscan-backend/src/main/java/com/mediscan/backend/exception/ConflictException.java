package com.mediscan.backend.exception;

/**
 * Thrown when a request conflicts with existing state (e.g. duplicate email on register).
 * Maps to HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
