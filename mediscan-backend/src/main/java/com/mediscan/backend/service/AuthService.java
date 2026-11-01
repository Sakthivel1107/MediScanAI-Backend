package com.mediscan.backend.service;

import com.mediscan.backend.dto.auth.AuthResponse;
import com.mediscan.backend.dto.auth.LoginRequest;
import com.mediscan.backend.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
