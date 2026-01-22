package vn.tt.practice.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.tt.practice.userservice.dto.UserLoginRequest;
import vn.tt.practice.userservice.dto.UserRegisterRequest;
import vn.tt.practice.userservice.dto.UserResponse;
import vn.tt.practice.userservice.model.User;
import vn.tt.practice.userservice.model.UserRole;
import vn.tt.practice.userservice.repository.UserRepository;
import vn.tt.practice.userservice.security.JwtTokenProvider;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RedisTemplate redisTemplate;

    @Mock
    private ValueOperations valueOperations;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void register_Success() {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("test@example.com")
                .password("password")
                .fullName("Test User")
                .phone("1234567890")
                .build();

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_Success() {
        UserLoginRequest request = UserLoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encodedPassword")
                .isActived(true)
                .role(UserRole.ROLE_USER)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("accessToken");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(refreshTokenService.create(user.getId())).thenReturn("refreshToken");

        var response = userService.login(request);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
    }
}
