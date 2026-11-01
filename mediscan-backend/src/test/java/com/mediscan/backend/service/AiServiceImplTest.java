package com.mediscan.backend.service;

import com.mediscan.backend.exception.AiServiceException;
import com.mediscan.backend.service.impl.AiServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the guard clauses in {@link AiServiceImpl}. Full end-to-end tests
 * against the live AI API are intentionally excluded from the unit test suite since
 * they require network access and a real API key; those are better suited to a
 * manual/integration test profile.
 */
class AiServiceImplTest {

    @Test
    void identifyMedicine_shouldThrow_whenApiKeyIsNotConfigured() {
        AiServiceImpl aiService = new AiServiceImpl(
                "https://api.anthropic.com/v1/messages", "", "claude-sonnet-4-6", 30000L);

        assertThatThrownBy(() -> aiService.identifyMedicine("Paracetamol 500mg"))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    void identifyMedicine_shouldThrow_whenExtractedTextIsBlank() {
        AiServiceImpl aiService = new AiServiceImpl(
                "https://api.anthropic.com/v1/messages", "fake-api-key", "claude-sonnet-4-6", 30000L);

        assertThatThrownBy(() -> aiService.identifyMedicine("   "))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("No text was extracted");
    }
}
