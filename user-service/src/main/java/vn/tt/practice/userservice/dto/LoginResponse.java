package vn.tt.practice.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String role;
}
