package com.mediscan.backend.service.impl;

import com.mediscan.backend.dto.user.UserResponse;
import com.mediscan.backend.exception.ResourceNotFoundException;
import com.mediscan.backend.model.User;
import com.mediscan.backend.repository.UserRepository;
import com.mediscan.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getCurrentUserProfile(String userId) {
        return toResponse(getUserEntityById(userId));
    }

    @Override
    public User getUserEntityById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
