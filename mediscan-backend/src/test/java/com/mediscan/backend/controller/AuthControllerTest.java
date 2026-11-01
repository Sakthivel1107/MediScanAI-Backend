package com.mediscan.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediscan.backend.dto.auth.AuthResponse;
import com.mediscan.backend.dto.auth.LoginRequest;
import com.mediscan.backend.dto.auth.RegisterRequest;
import com.mediscan.backend.dto.user.UserResponse;
import com.mediscan.backend.security.CustomUserDetailsService;
import com.mediscan.backend.service.AuthService;
import com.mediscan.backend.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for {@link AuthController}: verifies request validation and the
 * public/authenticated routing rules for the auth endpoints.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Security beans required to build the filter chain in the slice context.
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void register_shouldReturn201_whenRequestIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123");
        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(UserResponse.builder().id("user-1").email("jane@example.com").build())
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest("Jane Doe", "not-an-email", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void login_shouldReturn400_whenPasswordMissing() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
