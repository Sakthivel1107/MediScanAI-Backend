package com.mediscan.backend.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil(
            "this-is-a-test-secret-key-that-is-long-enough-for-hs256",
            3600000L * 24 * 30
    );

    @Test
    void generateToken_thenExtractSubject_shouldRoundTrip() {
        String token = jwtUtil.generateToken("user@example.com", Map.of("userId", "abc123"));

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractSubject(token)).isEqualTo("user@example.com");
    }

    @Test
    void isTokenValid_shouldReturnTrue_forMatchingSubjectAndUnexpiredToken() {
        String token = jwtUtil.generateToken("user@example.com", Map.of());

        assertThat(jwtUtil.isTokenValid(token, "user@example.com")).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forMismatchedSubject() {
        String token = jwtUtil.generateToken("user@example.com", Map.of());

        assertThat(jwtUtil.isTokenValid(token, "someone-else@example.com")).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forExpiredToken() {
        JwtUtil shortLivedJwtUtil = new JwtUtil(
                "this-is-a-test-secret-key-that-is-long-enough-for-hs256", -1000L);
        String expiredToken = shortLivedJwtUtil.generateToken("user@example.com", Map.of());

        assertThat(shortLivedJwtUtil.isTokenValid(expiredToken, "user@example.com")).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forMalformedToken() {
        assertThat(jwtUtil.isTokenValid("not-a-real-jwt", "user@example.com")).isFalse();
    }
}
