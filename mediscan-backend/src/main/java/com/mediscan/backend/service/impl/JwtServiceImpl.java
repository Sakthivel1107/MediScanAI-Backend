package com.mediscan.backend.service.impl;

import com.mediscan.backend.model.User;
import com.mediscan.backend.service.JwtService;
import com.mediscan.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtUtil jwtUtil;

    @Override
    public String generateToken(User user) {
        return jwtUtil.generateToken(user.getEmail(), Map.of(
                "userId", user.getId(),
                "name", user.getName()
        ));
    }

    @Override
    public long getExpirationMs() {
        return jwtUtil.getExpirationMs();
    }
}
