package com.mediscan.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_shouldReturn404_withErrorEnvelope() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new ResourceNotFoundException("Scan not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Scan not found");
        assertThat(response.getBody().getErrorCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    void handleBadRequest_shouldReturn400() {
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(new BadRequestException("Invalid image file"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void handleUnauthorized_shouldReturn401() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(new UnauthorizedException("Invalid credentials"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleForbidden_shouldReturn403() {
        ResponseEntity<ErrorResponse> response = handler.handleForbidden(new ForbiddenException("Access denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void handleConflict_shouldReturn409() {
        ResponseEntity<ErrorResponse> response = handler.handleConflict(new ConflictException("Email already exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleFileTooLarge_shouldReturn413() {
        ResponseEntity<ErrorResponse> response = handler.handleFileTooLarge(new FileTooLargeException("Too large"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @Test
    void handleAiError_shouldReturn503_withoutLeakingInternalDetails() {
        ResponseEntity<ErrorResponse> response = handler.handleAiError(new AiServiceException("connection refused to internal-ai-host:9999"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().getMessage()).doesNotContain("internal-ai-host");
    }

    @Test
    void handleGenericException_shouldReturn500_withoutLeakingStackTrace() {
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(new RuntimeException("db password is hunter2"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).doesNotContain("hunter2");
    }
}
