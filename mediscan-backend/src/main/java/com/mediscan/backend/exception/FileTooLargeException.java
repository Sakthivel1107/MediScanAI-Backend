package com.mediscan.backend.exception;

/**
 * Thrown when an uploaded file exceeds the configured maximum size.
 * Maps to HTTP 413.
 */
public class FileTooLargeException extends RuntimeException {

    public FileTooLargeException(String message) {
        super(message);
    }
}
