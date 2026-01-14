package vn.tt.practice.apigateway.security.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import vn.tt.practice.apigateway.exception.UnauthorizedException;

import java.util.List;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    private final JwtTokenValidator jwtTokenValidator;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public JwtAuthenticationFilter(JwtTokenValidator jwtTokenValidator, ReactiveRedisTemplate<String, String> redisTemplate) {
        this.jwtTokenValidator = jwtTokenValidator;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new UnauthorizedException("Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        
        // Check blacklist
        return redisTemplate.hasKey("blacklist:" + token)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return Mono.error(new UnauthorizedException("Token is blacklisted"));
                    }
                    
                    try {
                        Claims claims = jwtTokenValidator.validate(token);
                        
                        if (jwtTokenValidator.isTokenExpired(claims)) {
                             return Mono.error(new UnauthorizedException("Token expired"));
                        }

                        ServerHttpRequest request = exchange.getRequest()
                                .mutate()
                                .header("X-User-Id", claims.getSubject())
                                .header("X-User-Roles", String.join(",", claims.get("roles", List.class)))
                                .header("X-Internal-Gateway", "true")
                                .build();

                        return chain.filter(exchange.mutate().request(request).build());

                    } catch (Exception e) {
                        return Mono.error(new UnauthorizedException("Invalid token"));
                    }
                });
    }
}
