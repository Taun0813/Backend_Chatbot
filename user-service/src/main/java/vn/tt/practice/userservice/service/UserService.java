package vn.tt.practice.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.tt.practice.userservice.dto.LoginResponse;
import vn.tt.practice.userservice.dto.UserLoginRequest;
import vn.tt.practice.userservice.dto.UserRegisterRequest;
import vn.tt.practice.userservice.dto.UserResponse;
import vn.tt.practice.userservice.model.User;
import vn.tt.practice.userservice.model.UserRole;
import vn.tt.practice.userservice.repository.UserRepository;
import vn.tt.practice.userservice.security.JwtTokenProvider;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
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
    // Refresh Token

    // Get User Profile
    // Update User Profile
    // Admin Get User by Id
    // Admin Update User Role

    // List Address
    // Add Address
    // Update Address
    // Delete Address

}
