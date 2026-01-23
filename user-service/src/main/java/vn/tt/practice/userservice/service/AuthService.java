package vn.tt.practice.userservice.service;

import vn.tt.practice.userservice.dto.LoginRequest;
import vn.tt.practice.userservice.dto.LoginResponse;
import vn.tt.practice.userservice.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
}
