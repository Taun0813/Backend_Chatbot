package vn.tt.practice.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.tt.practice.apigateway.security.jwt.JwtAuthenticationFilter;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator routes(
            RouteLocatorBuilder builder,
            JwtAuthenticationFilter jwtFilter,
            KeyResolver userKeyResolver
    ) {
        return builder.routes()
        // ================= USER SERVICE =================
        // Public auth APIs: /users/auth/**
                .route("user_auth", r -> r.path("/users/auth/**")
                        .filters(f -> f.requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                                .setKeyResolver(userKeyResolver)))
                        .uri("lb://user-service"))

                // Protected user APIs: /users/**
                .route("user_service", r -> r.path("/users/**")
                        .filters(f -> f.filter(jwtFilter)
                                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver)))
                        .uri("lb://user-service"))

                // ================= PRODUCT SERVICE =================
                // Public GET products
                .route("product_public", r -> r.path("/products/**")
                        .and().method("GET")
                        .filters(f -> f.requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                                .setKeyResolver(userKeyResolver)))
                        .uri("lb://product-service"))

                // ================= CART SERVICE =================
                .route("cart_service", r -> r.path("/carts/**")
                        .filters(f -> f.filter(jwtFilter)
                                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver)))
                        .uri("lb://cart-service"))

                // ================= ORDER SERVICE =================
                .route("order_service", r -> r.path("/orders/**")
                        .filters(f -> f.filter(jwtFilter)
                                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())
                                        .setKeyResolver(userKeyResolver)))
                        .uri("lb://order-service"))

                .build();
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // defaultReplenishRate: số request mỗi giây
        // defaultBurstCapacity: số request tối đa trong 1 giây (burst)
        // defaultRequestedTokens: số token cần cho mỗi request (thường là 1)
        return new RedisRateLimiter(10, 20, 1);
    }
}
