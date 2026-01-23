//package vn.tt.practice.apigateway.filter;
//
//import io.jsonwebtoken.Claims;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//import vn.tt.practice.apigateway.util.ApiResponseUtil;
//import vn.tt.practice.apigateway.util.JwtUtil;
//
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
//
//    private final JwtUtil jwtUtil;
//
//    private static final List<String> EXCLUDED_PATHS = List.of(
//            "/api/auth/login",
//            "/api/auth/register",
//            "/api/auth/refresh",
//            "/actuator",
//            "/swagger-ui",
//            "/v3/api-docs"
//    );
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        String path = exchange.getRequest().getPath().toString();
//
//        // Skip JWT validation for excluded paths
//        if (isExcludedPath(path)) {
//            log.debug("Skipping JWT validation for path: {}", path);
//            return chain.filter(exchange);
//        }
//
//        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            log.warn("Missing or invalid Authorization header for path: {}", path);
//            return ApiResponseUtil.sendUnauthorizedResponse(
//                    exchange,
//                    "Missing or invalid Authorization header"
//            );
//        }
//
//        String token = authHeader.substring(7);
//
//        try {
//            Claims claims = jwtUtil.validateToken(token);
//
//            if (jwtUtil.isTokenExpired(claims)) {
//                log.warn("Expired token for path: {}", path);
//                return ApiResponseUtil.sendUnauthorizedResponse(
//                        exchange,
//                        "Token has expired"
//                );
//            }
//
//            // Extract user information from JWT
//            String userId = jwtUtil.getUserId(claims);
//            String email = jwtUtil.getEmail(claims);
//            List<String> roles = jwtUtil.getRoles(claims);
//
//            // Add headers for downstream services
//            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
//                    .header("X-User-Id", userId)
//                    .header("X-User-Email", email)
//                    .header("X-User-Roles", String.join(",", roles))
//                    .build();
//
//            log.debug("JWT validated for user: {} ({})", email, userId);
//
//            return chain.filter(exchange.mutate().request(mutatedRequest).build());
//
//        } catch (Exception e) {
//            log.error("JWT validation failed: {}", e.getMessage());
//            return ApiResponseUtil.sendUnauthorizedResponse(
//                    exchange,
//                    "Invalid or malformed JWT token"
//            );
//        }
//    }
//
//    private boolean isExcludedPath(String path) {
//        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
//    }
//
//    @Override
//    public int getOrder() {
//        return -100; // Run after RequestIdFilter but before other filters
//    }
//}
