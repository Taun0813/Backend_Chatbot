package vn.tt.practice.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.userservice.dto.LoginRequest;
import vn.tt.practice.userservice.dto.LoginResponse;
import vn.tt.practice.userservice.dto.RegisterRequest;
import vn.tt.practice.userservice.entity.Role;
import vn.tt.practice.userservice.entity.User;
import vn.tt.practice.userservice.entity.UserProfile;
import vn.tt.practice.userservice.repository.UserProfileRepository;
import vn.tt.practice.userservice.repository.UserRepository;
import vn.tt.practice.userservice.security.CustomUserDetails;
import vn.tt.practice.userservice.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        userProfileRepository.save(profile);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        return LoginResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .type("Bearer")
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // Simple logic: Verify signature, get ID, load user, generate new token
        // In real world we should verify if refresh token actually belongs to user in DB or Redis
        try {
            String userId = jwtTokenProvider.getUserNameFromJwtToken(refreshToken);
            User user = userRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Manually re-create detail to generate token
            CustomUserDetails customUserDetails = new CustomUserDetails(user);
            // We can't use Authentication object easily here without full re-auth flow or mocking it
            // So we override generateToken to accept user directly or just create a dummy auth object?
            // Let's refactor generateToken to be flexible? Or just use the map overload.
            
            String newAccessToken = jwtTokenProvider.generateToken(new java.util.HashMap<>(), user.getId(), user.getRole().name());
            String newRefreshToken = refreshToken; // Rotate or keep? Keep for now.

            return LoginResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .id(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .type("Bearer")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token");
        }
    }
}
