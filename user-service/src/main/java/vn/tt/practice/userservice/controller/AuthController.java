package vn.tt.practice.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.userservice.dto.*;
import vn.tt.practice.userservice.service.AuthService;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "APIs for user authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register user")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody UserRegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build())
                .build());
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody UserLoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .data(response)
                .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build())
                .build());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token (alias)")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        LoginResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.<LoginResponse>builder()
                .data(response)
                .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build())
                .build());
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshTokenEndpoint(@RequestBody Map<String, String> request) {
        return refresh(request);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        
        if (accessToken != null && userId != null) {
            authService.logout(accessToken, userId);
        }
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build())
                .build());
    }
}
