package com.mediscan.backend.service;

import com.mediscan.backend.dto.user.UserResponse;
import com.mediscan.backend.enums.AuthProvider;
import com.mediscan.backend.exception.ResourceNotFoundException;
import com.mediscan.backend.model.User;
import com.mediscan.backend.repository.UserRepository;
import com.mediscan.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getCurrentUserProfile_shouldReturnProfile_whenUserExists() {
        User user = User.builder()
                .id("user-1").name("Jane").email("jane@example.com").provider(AuthProvider.LOCAL).build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        UserResponse response = userService.getCurrentUserProfile("user-1");

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getName()).isEqualTo("Jane");
    }

    @Test
    void getCurrentUserProfile_shouldThrowNotFound_whenUserMissing() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUserProfile("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
