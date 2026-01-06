package vn.tt.practice.apigateway.config;

import org.springframework.beans.factory.annotation.Value;

public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    private String header = "Authorization";

    private String prefix = "Bearer";
}
