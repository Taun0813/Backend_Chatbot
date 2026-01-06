package vn.tt.practice.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tt.practice.userservice.dto.LoginResponse;
import vn.tt.practice.userservice.dto.UserLoginRequest;
import vn.tt.practice.userservice.dto.UserRegisterRequest;
import vn.tt.practice.userservice.dto.UserResponse;
import vn.tt.practice.userservice.service.UserService;

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

    // POST : /users/register
    @PostMapping("/auth/register")
    public UserResponse register(@RequestBody UserRegisterRequest request) {
        return userService.register(request);
    }

    // POST : /auth/refresh-token




    // GET : /users/me
    // PUT : /users/me
    // GET : /users/{id}
    // PUT : /users
}
