package vn.tt.practice.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service-auth", r -> r
                        .path("/api/auth/**", "/api/users/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .circuitBreaker(c -> c
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user-service")))
                        .uri("lb://user-service"))

                // Product Service Routes
                .route("product-service", r -> r
                        .path("/api/products/**", "/api/categories/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .circuitBreaker(c -> c
                                        .setName("productServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/product-service")))
                        .uri("lb://product-service"))

                // Cart Service Routes
                .route("cart-service", r -> r
                        .path("/api/carts/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .circuitBreaker(c -> c
                                        .setName("cartServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/cart-service")))
                        .uri("lb://cart-service"))

                // Order Service Routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .circuitBreaker(c -> c
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/order-service")))
                        .uri("lb://order-service"))

                // Inventory Service Routes
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .circuitBreaker(c -> c
                                        .setName("inventoryServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/inventory-service")))
                        .uri("lb://inventory-service"))

                // Payment Service Routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .circuitBreaker(c -> c
                                        .setName("paymentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/payment-service")))
                        .uri("lb://payment-service"))

                // Warranty Service Routes
                .route("warranty-service", r -> r
                        .path("/api/warranties/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .circuitBreaker(c -> c
                                        .setName("warrantyServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/warranty-service")))
                        .uri("lb://warranty-service"))

                // Recommendation Service Routes
                .route("recommendation-service", r -> r
                        .path("/api/recommendations/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<segment>.*)", "/${segment}")
                                .circuitBreaker(c -> c
                                        .setName("recommendationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/recommendation-service")))
                        .uri("lb://recommendation-service"))

                .build();
    }
}
