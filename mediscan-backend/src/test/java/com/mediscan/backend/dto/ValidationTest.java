package com.mediscan.backend.dto;

import com.mediscan.backend.dto.auth.LoginRequest;
import com.mediscan.backend.dto.auth.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies Jakarta Bean Validation constraints on request DTOs.
 */
class ValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void registerRequest_shouldFailValidation_whenEmailIsInvalid() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "not-an-email", "password123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void registerRequest_shouldFailValidation_whenPasswordTooShort() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "short");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void registerRequest_shouldFailValidation_whenNameIsBlank() {
        RegisterRequest request = new RegisterRequest("", "jane@example.com", "password123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void registerRequest_shouldPassValidation_whenAllFieldsValid() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void loginRequest_shouldFailValidation_whenPasswordBlank() {
        LoginRequest request = new LoginRequest("jane@example.com", "");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
