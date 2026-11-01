package com.mediscan.backend.service.impl;

import com.mediscan.backend.dto.auth.AuthResponse;
import com.mediscan.backend.dto.auth.LoginRequest;
import com.mediscan.backend.dto.auth.RegisterRequest;
import com.mediscan.backend.enums.AuthProvider;
import com.mediscan.backend.exception.ConflictException;
import com.mediscan.backend.exception.UnauthorizedException;
import com.mediscan.backend.model.User;
import com.mediscan.backend.repository.UserRepository;
import com.mediscan.backend.service.AuthService;
import com.mediscan.backend.service.JwtService;
import com.mediscan.backend.service.UserService;
import com.mediscan.backend.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = ValidationUtil.normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.LOCAL)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {}", saved.getId());

        return buildAuthResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = ValidationUtil.normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getId());
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .message("Login successful")
                .user(userService.toResponse(user))
                .build();
    }
}
