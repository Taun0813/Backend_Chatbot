package vn.tt.practice.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.userservice.dto.LoginRequest;
import vn.tt.practice.userservice.dto.LoginResponse;
import vn.tt.practice.userservice.dto.RegisterRequest;
import vn.tt.practice.userservice.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token")
    public ResponseEntity<LoginResponse> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
