package com.mediscan.backend.service;

import com.mediscan.backend.dto.user.UserResponse;
import com.mediscan.backend.model.User;

public interface UserService {

    UserResponse getCurrentUserProfile(String userId);

    User getUserEntityById(String userId);

    UserResponse toResponse(User user);
}
