package vn.tt.practice.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.userservice.dto.*;
import vn.tt.practice.userservice.service.AddressService;
import vn.tt.practice.userservice.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "APIs for user authentication and management")
public class UserController {

    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private final UserService userService;
    private final AddressService addressService;

    private static Set<String> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) return Set.of();
        return Stream.of(rolesHeader.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    private static boolean hasAdminOrSuperAdmin(Set<String> roles) {
        return roles.contains(ROLE_ADMIN) || roles.contains(ROLE_SUPER_ADMIN);
    }

    private static boolean hasSuperAdmin(Set<String> roles) {
        return roles.contains(ROLE_SUPER_ADMIN);
    }

    // Admin endpoints (ADMIN or SUPER_ADMIN)

    @GetMapping
    @Operation(summary = "Get all users (ADMIN only)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            Pageable pageable,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader) {
        if (!hasAdminOrSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Page<UserResponse>>builder()
                            .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build()).build());
        }
        Page<UserResponse> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<UserResponse>>builder()
                .data(response)
                .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build())
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID (ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader) {
        if (!hasAdminOrSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<UserResponse>builder()
                            .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build()).build());
        }
        return ok(userService.getUserById(id));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader) {
        if (!hasSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<UserResponse>builder()
                            .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build()).build());
        }
        String role = request.get("role");
        UserResponse response = userService.updateUserRole(id, role);
        return ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = HEADER_USER_ROLES, required = false) String rolesHeader) {
        if (!hasSuperAdmin(parseRoles(rolesHeader))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.<Void>builder()
                            .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build()).build());
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .meta(Meta.builder().timestamp(LocalDateTime.now().toString()).build())
                .build());
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
