package com.mediscan.backend.exception;

/**
 * Thrown when a requested resource (user, scan, medicine, etc.) does not exist
 * or does not belong to the requesting user. Maps to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
