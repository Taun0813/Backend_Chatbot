package vn.tt.practice.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tt.practice.userservice.dto.UserResponse;
import vn.tt.practice.userservice.service.UserService;
import vn.tt.practice.userservice.util.StandardResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info")
    public ResponseEntity<StandardResponse<UserResponse>> getCurrentUser() {
        return ResponseEntity.ok(
                StandardResponse.success(userService.getCurrentUser(), "Current user info")
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.isSameUser(#id)") 
    // Note: complex authorization might need custom logic, keeping simple for now
    @Operation(summary = "Get user by ID")
    public ResponseEntity<StandardResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                StandardResponse.success(userService.getUserById(id), "User info found")
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<StandardResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(
                StandardResponse.success(userService.getAllUsers(), "List of all users")
        );
    }
}
