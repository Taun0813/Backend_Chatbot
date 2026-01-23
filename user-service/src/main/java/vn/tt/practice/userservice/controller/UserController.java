package vn.tt.practice.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.tt.practice.userservice.dto.UserDTO;
import vn.tt.practice.userservice.dto.UserProfileDTO;
import vn.tt.practice.userservice.security.CustomUserDetails;
import vn.tt.practice.userservice.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User Management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUser().getId()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserDTO> updateCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @RequestBody UserProfileDTO request) {
        return ResponseEntity.ok(userService.updateUserProfile(userDetails.getUser().getId(), request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get user by ID (Admin only)")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
