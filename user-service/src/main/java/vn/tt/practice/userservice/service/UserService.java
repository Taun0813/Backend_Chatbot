package vn.tt.practice.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.userservice.dto.*;
import vn.tt.practice.userservice.model.Address;
import vn.tt.practice.userservice.model.RefreshToken;
import vn.tt.practice.userservice.model.User;
import vn.tt.practice.userservice.model.UserRole;
import vn.tt.practice.userservice.repository.AddressRepository;
import vn.tt.practice.userservice.repository.UserRepository;
import vn.tt.practice.userservice.security.JwtTokenProvider;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;


    // Register User
    public UserResponse register(UserRegisterRequest request) {
        try {
            User user = User.builder()
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .fullName(request.getFullName())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(UserRole.USER)
                    .isActived(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            UserResponse response = UserResponse.builder()
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .fullName(request.getFullName())
                    .role("USER")
                    .isActived(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(user);

            return response;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
    // Login User
    public LoginResponse login(UserLoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isActived()) {
            throw new RuntimeException("User is not activated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = refreshTokenService.create(user.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                3600
        );
    }

    // Logout User
    public void logout(UUID userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    // Refresh Token
    public LoginResponse refreshToken(String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserId)
                .map(userId -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    String token = jwtTokenProvider.generateAccessToken(user);
                    return new LoginResponse(token, requestRefreshToken, "Bearer", 3600);
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    // Get User Profile
    public UserResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    // Update User Profile
    public UserResponse updateUserProfile(UUID userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);
        return mapToUserResponse(user);
    }

    // Admin Get User by Id
    public UserResponse getUserById(UUID userId) {
        return getUserProfile(userId);
    }

    // Admin Update User Role
    public UserResponse updateUserRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            UserRole role = UserRole.valueOf(roleName.toUpperCase());
            user.setRole(role);
            userRepository.save(user);
            return mapToUserResponse(user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role name");
        }
    }

    // List Address
    public List<AddressResponse> getUserAddresses(UUID userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    // Add Address
    @Transactional
    public AddressResponse addAddress(UUID userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            // Unset other default addresses
            List<Address> addresses = addressRepository.findByUserId(userId);
            addresses.forEach(a -> {
                if (Boolean.TRUE.equals(a.getIsDefault())) {
                    a.setIsDefault(false);
                    addressRepository.save(a);
                }
            });
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .district(request.getDistrict())
                .ward(request.getWard())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        addressRepository.save(address);
        return mapToAddressResponse(address);
    }

    // Update Address
    @Transactional
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Address does not belong to user");
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
             List<Address> addresses = addressRepository.findByUserId(userId);
            addresses.forEach(a -> {
                if (!a.getId().equals(addressId) && Boolean.TRUE.equals(a.getIsDefault())) {
                    a.setIsDefault(false);
                    addressRepository.save(a);
                }
            });
        }

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        addressRepository.save(address);
        return mapToAddressResponse(address);
    }

    // Delete Address
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Address does not belong to user");
        }

        addressRepository.delete(address);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .isActived(user.isActived())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .district(address.getDistrict())
                .ward(address.getWard())
                .isDefault(address.getIsDefault())
                .build();
    }
}
