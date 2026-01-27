package vn.tt.practice.userservice.service;

import vn.tt.practice.userservice.dto.LoginResponse;
import vn.tt.practice.userservice.dto.UserLoginRequest;
import vn.tt.practice.userservice.dto.UserRegisterRequest;

import java.util.UUID;

public interface AuthService {
    void register(UserRegisterRequest request);
    LoginResponse login(UserLoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    void logout(String accessToken, UUID userId);
}
