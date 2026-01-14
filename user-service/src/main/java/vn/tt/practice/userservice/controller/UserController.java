package vn.tt.practice.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.userservice.dto.*;
import vn.tt.practice.userservice.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "APIs for user authentication and management")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Login user", description = "Authenticate user and return access token & refresh token")
    @PostMapping("/auth/login")
    public LoginResponse login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

    @Operation(summary = "Register user", description = "Register a new user")
    @PostMapping("/auth/register")
    public UserResponse register(@RequestBody UserRegisterRequest request) {
        return userService.register(request);
    }

    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    @PostMapping("/auth/refresh-token")
    public LoginResponse refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        return userService.refreshToken(refreshToken);
    }

    @Operation(summary = "Logout user", description = "Logout user and invalidate refresh token")
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-User-Id") UUID userId) {
        userService.logout(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get current user profile", description = "Get profile of the currently logged in user")
    @GetMapping("/me")
    public UserResponse getCurrentUser(@RequestHeader("X-User-Id") UUID userId) {
        return userService.getUserProfile(userId);
    }

    @Operation(summary = "Update current user profile", description = "Update profile of the currently logged in user")
    @PutMapping("/me")
    public UserResponse updateCurrentUser(@RequestHeader("X-User-Id") UUID userId, @RequestBody UserProfileUpdateRequest request) {
        return userService.updateUserProfile(userId, request);
    }

    @Operation(summary = "Get user addresses", description = "Get list of addresses for the current user")
    @GetMapping("/me/addresses")
    public List<AddressResponse> getUserAddresses(@RequestHeader("X-User-Id") UUID userId) {
        return userService.getUserAddresses(userId);
    }

    @Operation(summary = "Add new address", description = "Add a new address for the current user")
    @PostMapping("/me/addresses")
    public AddressResponse addAddress(@RequestHeader("X-User-Id") UUID userId, @RequestBody AddressRequest request) {
        return userService.addAddress(userId, request);
    }

    @Operation(summary = "Update address", description = "Update an existing address")
    @PutMapping("/me/addresses/{addressId}")
    public AddressResponse updateAddress(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID addressId,
            @RequestBody AddressRequest request) {
        return userService.updateAddress(userId, addressId, request);
    }

    @Operation(summary = "Delete address", description = "Delete an existing address")
    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID addressId) {
        userService.deleteAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }

    // Admin APIs
    @Operation(summary = "Get user by ID (Admin)", description = "Get user profile by ID (Admin only)")
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Update user role (Admin)", description = "Update user role (Admin only)")
    @PutMapping("/{id}/role")
    public UserResponse updateUserRole(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        String role = request.get("role");
        return userService.updateUserRole(id, role);
    }
}
