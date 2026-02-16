package vn.tt.practice.apigateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import vn.tt.practice.apigateway.config.JwtConfig;
import vn.tt.practice.apigateway.dto.ApiResponse;
import vn.tt.practice.apigateway.util.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Global filter: extract JWT from Authorization header, validate signature and expiration,
 * extract user id and roles, forward via headers. Allow /api/auth/** without token.
 * Block invalid/expired tokens with 401.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String CODE_UNAUTHORIZED = "UNAUTHORIZED";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final JwtUtil jwtUtil;
    private final JwtConfig jwtConfig;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Allow /api/auth/** without token
        if (path.startsWith("/api/auth/")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(jwtConfig.getPrefix())) {
            return unauthorized(exchange, "Missing or invalid Authorization header", path);
        }

        String token = authHeader.substring(jwtConfig.getPrefix().length()).trim();
        if (token.isEmpty()) {
            return unauthorized(exchange, "Empty token", path);
        }

        try {
            jwtUtil.validateToken(token);
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return unauthorized(exchange, "Invalid or expired token", path);
        }

        String userId = jwtUtil.extractUserId(token);
        List<String> roles = jwtUtil.extractRoles(token);
        String rolesHeader = roles != null ? String.join(",", roles) : "";

        ServerWebExchange mutated = exchange.mutate()
                .request(b -> b.header("X-User-Id", userId != null ? userId : "")
                        .header("X-User-Roles", rolesHeader))
                .build();

        return chain.filter(mutated);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message, String path) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ApiResponse<?> body = ApiResponse.error(message, CODE_UNAUTHORIZED, path,
                exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER));
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ApiResponse", e);
            bytes = ("{\"success\":false,\"message\":\"" + message + "\",\"code\":\"" + CODE_UNAUTHORIZED + "\"}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -2; // Run before RateLimitFilter (-1) so X-User-Id is set for rate limit
    }
}
