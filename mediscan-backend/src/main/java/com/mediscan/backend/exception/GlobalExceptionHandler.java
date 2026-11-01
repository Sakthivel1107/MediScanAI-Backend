package com.mediscan.backend.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handling for the whole application.
 *
 * <p>Ensures every error response follows the standard {@link ErrorResponse} envelope
 * and never leaks stack traces, secrets, or internal implementation details to clients.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), "NOT_FOUND", null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), "BAD_REQUEST", null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), "UNAUTHORIZED", null);
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", "AUTHENTICATION_FAILED", null);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        log.warn("Forbidden: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), "FORBIDDEN", null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), "CONFLICT", null);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateKey(DuplicateKeyException ex) {
        log.warn("Duplicate key violation");
        return build(HttpStatus.CONFLICT, "A record with these details already exists", "DUPLICATE_KEY", null);
    }

    @ExceptionHandler({FileTooLargeException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ErrorResponse> handleFileTooLarge(Exception ex) {
        log.warn("File too large: {}", ex.getMessage());
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "Uploaded file exceeds the maximum allowed size",
                "PAYLOAD_TOO_LARGE", null);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException ex) {
        log.warn("Missing request part: {}", ex.getRequestPartName());
        return build(HttpStatus.BAD_REQUEST, "Required part '" + ex.getRequestPartName() + "' is missing",
                "MISSING_PART", null);
    }

    @ExceptionHandler(OcrProcessingException.class)
    public ResponseEntity<ErrorResponse> handleOcrError(OcrProcessingException ex) {
        log.error("OCR processing failed: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Could not read text from the uploaded image",
                "OCR_FAILED", null);
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiError(AiServiceException ex) {
        log.error("AI service failed: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "The AI identification service is currently unavailable. "
                + "Please try again shortly.", "AI_SERVICE_UNAVAILABLE", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validation failed: {}", details);
        return build(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_ERROR", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_ERROR", null);
    }

    @ExceptionHandler(UncategorizedMongoDbException.class)
    public ResponseEntity<ErrorResponse> handleMongoError(UncategorizedMongoDbException ex) {
        log.error("Database error occurred", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred. Please try again later.",
                "DATABASE_ERROR", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_ARGUMENT", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.",
                "INTERNAL_ERROR", null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String errorCode,
                                                 Map<String, String> details) {
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .details(details)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
