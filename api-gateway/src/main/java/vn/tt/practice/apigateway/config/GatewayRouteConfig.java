package vn.tt.practice.apigateway.config;

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
            JwtAuthenticationFilter jwtFilter
    ) {
        return builder.routes()
        // ================= USER SERVICE =================
        // Public auth APIs: /users/auth/**
                .route("user_auth", r -> r.path("/users/auth/**")
                .uri("lb://user-service"))

                // Protected user APIs: /users/**
                .route("user_service", r -> r.path("/users/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://user-service"))

                // ================= PRODUCT SERVICE =================
                // Public GET products
                .route("product_public", r -> r.path("/products/**")
                        .and().method("GET")
                        .uri("lb://product-service"))

                // ================= CART SERVICE =================
                .route("cart_service", r -> r.path("/carts/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://cart-service"))

                // ================= ORDER SERVICE =================
                .route("order_service", r -> r.path("/orders/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri("lb://order-service"))

                .build();
    }
}
