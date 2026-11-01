package com.mediscan.backend.controller;

import com.mediscan.backend.dto.common.ApiResponse;
import com.mediscan.backend.dto.user.UserResponse;
import com.mediscan.backend.security.CustomUserDetails;
import com.mediscan.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "Authenticated user profile")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile", description = "Returns the authenticated user's profile.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UserResponse response = userService.getCurrentUserProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.of("User profile retrieved successfully", response));
    }
}
