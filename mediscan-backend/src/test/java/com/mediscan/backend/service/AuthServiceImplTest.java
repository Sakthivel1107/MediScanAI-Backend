package com.mediscan.backend.service;

import com.mediscan.backend.dto.auth.AuthResponse;
import com.mediscan.backend.dto.auth.LoginRequest;
import com.mediscan.backend.dto.auth.RegisterRequest;
import com.mediscan.backend.dto.user.UserResponse;
import com.mediscan.backend.enums.AuthProvider;
import com.mediscan.backend.exception.ConflictException;
import com.mediscan.backend.exception.UnauthorizedException;
import com.mediscan.backend.model.User;
import com.mediscan.backend.repository.UserRepository;
import com.mediscan.backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id("user-1")
                .name("Jane Doe")
                .email("jane@example.com")
                .password("hashed-password")
                .provider(AuthProvider.LOCAL)
                .build();
    }

    @Test
    void register_shouldCreateNewUser_whenEmailNotTaken() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(jwtService.generateToken(existingUser)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);
        when(userService.toResponse(existingUser)).thenReturn(
                UserResponse.builder().id("user-1").name("Jane Doe").email("jane@example.com").build());

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void register_shouldThrowConflict_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "jane@example.com", "password123");
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("jane@example.com", "password123");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(existingUser)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);
        when(userService.toResponse(existingUser)).thenReturn(
                UserResponse.builder().id("user-1").email("jane@example.com").build());

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_shouldThrowUnauthorized_whenPasswordIncorrect() {
        LoginRequest request = new LoginRequest("jane@example.com", "wrong-password");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_shouldThrowUnauthorized_whenUserDoesNotExist() {
        LoginRequest request = new LoginRequest("missing@example.com", "password123");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);
    }
}
