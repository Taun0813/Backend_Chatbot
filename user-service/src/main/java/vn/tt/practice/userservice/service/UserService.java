package vn.tt.practice.userservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.tt.practice.userservice.dto.UserProfileUpdateRequest;
import vn.tt.practice.userservice.dto.UserResponse;

import java.util.UUID;

public interface UserService {
    UserResponse getCurrentUser(Long userId);
    UserResponse updateUserProfile(Long userId, UserProfileUpdateRequest request);
    // Admin methods
    UserResponse getUserById(Long id);
    Page<UserResponse> getAllUsers(Pageable pageable);
    UserResponse updateUserRole(Long userId, String role);
}
