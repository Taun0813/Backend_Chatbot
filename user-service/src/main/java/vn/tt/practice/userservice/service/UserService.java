package vn.tt.practice.userservice.service;

import vn.tt.practice.userservice.dto.UserDTO;
import vn.tt.practice.userservice.dto.UserProfileDTO;

public interface UserService {
    UserDTO getCurrentUser(Long userId);
    UserDTO updateUserProfile(Long userId, UserProfileDTO request);
    // Admin methods
    UserDTO getUserById(Long id);
}
