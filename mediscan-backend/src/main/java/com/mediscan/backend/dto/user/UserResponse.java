package com.mediscan.backend.dto.user;

import com.mediscan.backend.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Public-facing representation of a User (never includes the password hash).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String profileImage;
    private Instant createdAt;
}
