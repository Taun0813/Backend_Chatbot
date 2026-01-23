package vn.tt.practice.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.userservice.dto.*;
import vn.tt.practice.userservice.service.AddressService;
import vn.tt.practice.userservice.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "APIs for user authentication and management")
public class UserController {

    private final UserService userService;
    private final AddressService addressService;

    // Admin endpoints

    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String role = request.get("role"); // Assuming json is {"role": "ADMIN"} or similar, doc says "additionalProperties: string"
        // Actually doc requestBody content is generic map.
        
        UserResponse response = userService.updateUserRole(id, role);
        return ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all users (Admin)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(Pageable pageable) {
        Page<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<UserResponse>>builder()
                .data(response)
                .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build())
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (Admin)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ok(userService.getUserById(id));
    }

    // Current User endpoints

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        return ok(userService.getCurrentUser(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UserProfileUpdateRequest request) {
        return ok(userService.updateUserProfile(userId, request));
    }

    // Address endpoints

    @GetMapping("/me/addresses")
    @Operation(summary = "Get user addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getUserAddresses(@RequestHeader("X-User-Id") Long userId) {
        List<AddressResponse> response = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(ApiResponse.<List<AddressResponse>>builder()
                .data(response)
                .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build())
                .build());
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add new address")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AddressRequest request) {
        return ok(addressService.addAddress(userId, request));
    }

    @PutMapping("/me/addresses/{addressId}")
    @Operation(summary = "Update address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId,
            @RequestBody AddressRequest request) {
        return ok(addressService.updateAddress(userId, addressId, request));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @Operation(summary = "Delete address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long addressId) {
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build())
                .build());
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.<T>builder()
                .data(data)
                .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build())
                .build());
    }
}
