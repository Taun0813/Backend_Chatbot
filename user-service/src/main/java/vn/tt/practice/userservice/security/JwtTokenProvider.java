package vn.tt.practice.userservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import vn.tt.practice.userservice.config.JwtConfig;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        Long userId;
        String role;

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;
            userId = userDetails.getUser().getId();
            role = userDetails.getUser().getRole().name();
        } else {
             // Fallback or Exception, usually CustomUserDetails is enforced by Authentication Manager configuration
             throw new RuntimeException("Unexpected Principal Type");
        }
        
        return generateToken(new HashMap<>(), userId, role);
    }

    public String generateToken(Map<String, Object> extraClaims, Long userId, String role) {
        extraClaims.put("roles", role);
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userId.toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(getSignKey())
                .compact();
    }
    
    public String generateRefreshToken(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        Long userId;

        if (principal instanceof CustomUserDetails) {
             userId = ((CustomUserDetails) principal).getUser().getId();
        } else {
             throw new RuntimeException("Unexpected Principal Type");
        }
        
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getRefreshExpiration()))
                .signWith(getSignKey())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }
}
