package vn.tt.practice.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.userservice.dto.UserDTO;
import vn.tt.practice.userservice.dto.UserProfileDTO;
import vn.tt.practice.userservice.entity.User;
import vn.tt.practice.userservice.entity.UserProfile;
import vn.tt.practice.userservice.repository.UserProfileRepository;
import vn.tt.practice.userservice.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        return mapToDTO(user, profile);
    }

    @Override
    @Transactional
    public UserDTO updateUserProfile(Long userId, UserProfileDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().user(user).build());
        
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());
        profile.setCity(request.getCity());
        profile.setCountry(request.getCountry());
        profile.setPostalCode(request.getPostalCode());
        
        userProfileRepository.save(profile);
        return mapToDTO(user, profile);
    }

    @Override
    public UserDTO getUserById(Long id) {
        return getCurrentUser(id);
    }

    private UserDTO mapToDTO(User user, UserProfile profile) {
        UserProfileDTO profileDTO = null;
        if (profile != null) {
            profileDTO = UserProfileDTO.builder()
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .phone(profile.getPhone())
                    .address(profile.getAddress())
                    .city(profile.getCity())
                    .country(profile.getCountry())
                    .postalCode(profile.getPostalCode())
                    .build();
        }

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .profile(profileDTO)
                .build();
    }
}
