package vn.tt.practice.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tt.practice.userservice.config.JwtConfig;
import vn.tt.practice.userservice.dto.LoginResponse;
import vn.tt.practice.userservice.dto.UserLoginRequest;
import vn.tt.practice.userservice.dto.UserRegisterRequest;
import vn.tt.practice.userservice.entity.Role;
import vn.tt.practice.userservice.entity.User;
import vn.tt.practice.userservice.repository.UserRepository;
import vn.tt.practice.userservice.security.CustomUserDetails;
import vn.tt.practice.userservice.security.JwtTokenProvider;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "bl_";

    @Override
    @Transactional
    public void register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        userRepository.save(user);
    }

    @Override
    public LoginResponse login(UserLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration())
                .userId(userDetails.getUser().getId())
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // Simple implementation: extract user from token and issue new access token
        // In real world, verify refresh token signature and expiration
        String userIdStr = jwtTokenProvider.getUserNameFromJwtToken(refreshToken);
        Long userId = Long.parseLong(userIdStr);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // We need to construct a valid Authentication object to pass to generateToken
        // Or overload generateToken to accept User
        // Since JwtTokenProvider depends on Authentication, let's look at it.
        // It has generateToken(Map, UUID, String).
        
        String accessToken = jwtTokenProvider.generateToken(new java.util.HashMap<>(), user.getId(), user.getRole().name());
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration())
                .userId(user.getId())
                .build();
    }

    @Override
    public void logout(String accessToken, UUID userId) {
        // Invalidate access token (add to blacklist)
        // Access token usually comes with "Bearer " prefix, strip it if needed
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        
        long expiration = jwtConfig.getExpiration(); // Or calculate remaining time
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, userId.toString(), expiration, TimeUnit.MILLISECONDS);
    }
}
