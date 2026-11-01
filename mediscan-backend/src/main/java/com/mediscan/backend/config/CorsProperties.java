package com.mediscan.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Binds the comma-separated {@code cors.allowed-origins} property into a usable list.
 * Keeps CORS configuration driven entirely by environment variables (ALLOWED_ORIGINS),
 * never hardcoded or wide-open in production.
 */
@Component
public class CorsProperties {

    private final String allowedOrigins;

    public CorsProperties(@Value("${cors.allowed-origins}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> allowedOriginsList() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
