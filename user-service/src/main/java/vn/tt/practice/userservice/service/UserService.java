package vn.tt.practice.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.userservice.dto.UserProfileUpdateRequest;
import vn.tt.practice.userservice.dto.UserResponse;
import vn.tt.practice.userservice.model.Role;
import vn.tt.practice.userservice.model.User;
import vn.tt.practice.userservice.model.UserProfile;
import vn.tt.practice.userservice.repository.UserProfileRepository;
import vn.tt.practice.userservice.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;


    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public UserResponse getCurrentUser(Long userId) {
        User user = getUserEntity(userId);
        return mapToResponse(user);
    }


    @Transactional
    @CachePut(value = "users", key = "#userId")
    public UserResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = getUserEntity(userId);
        UserProfile profile = user.getUserProfile();
        
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
            user.setUserProfile(profile);
        }

        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        
        // Save user which cascades to profile
        user = userRepository.save(user);
        return mapToResponse(user);
    }


    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserResponse getUserById(Long id) {
        // Same implementation as getCurrentUser, but distinct for separation of concerns if needed.
        // Caching key is the same.
        User user = getUserEntity(id);
        return mapToResponse(user);
    }


    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }


    @Transactional
    @CachePut(value = "users", key = "#userId")
    public UserResponse updateUserRole(Long userId, String roleName) {
        User user = getUserEntity(userId);
        try {
            Role role = Role.valueOf(roleName);
            user.setRole(role);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + roleName);
        }
        
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserResponse mapToResponse(User user) {
        String fullName = null;
        String phone = null;
        if (user.getUserProfile() != null) {
            fullName = user.getUserProfile().getFullName();
            phone = user.getUserProfile().getPhone();
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(fullName)
                .phone(phone)
                .role(user.getRole().name())
//                .createdAt(user.getCreatedAt())
                .actived(user.getIsActive())
                .build();
    }
}
