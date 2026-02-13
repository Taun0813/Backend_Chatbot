package vn.tt.practice.apigateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import vn.tt.practice.apigateway.util.JwtUtil;

import java.util.List;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        
        // Allow OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method)) {
            return chain.filter(exchange);
        }
        
        // Allow auth endpoints without token
        if (path.startsWith("/api/auth/")) {
            return chain.filter(exchange);
        }

        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // Check blacklist
        return redisTemplate.hasKey("blacklist:" + token)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return onError(exchange, "Token is blacklisted", HttpStatus.UNAUTHORIZED);
                    }

                    try {
                        jwtUtil.validateToken(token);
                        String userId = jwtUtil.extractUserId(token);
                        List<String> roles = jwtUtil.extractRoles(token);

                        ServerHttpRequest request = exchange.getRequest().mutate()
                                .header("X-User-Id", userId)
                                .header("X-User-Roles", String.join(",", roles)) // Simple CSV format
                                .build();

                        return chain.filter(exchange.mutate().request(request).build());
                    } catch (Exception e) {
                        log.error("JWT validation error: {}", e.getMessage());
                        return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
                    }
                });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        String body = String.format("{\"success\":false,\"message\":\"%s\",\"timestamp\":\"%s\"}", 
                err, java.time.LocalDateTime.now());
        org.springframework.core.io.buffer.DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes());
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -2; // Run before RateLimitFilter (-1)
    }
}
