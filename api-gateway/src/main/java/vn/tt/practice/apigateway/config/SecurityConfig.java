package vn.tt.practice.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(ex -> ex
                        // Cho phép OPTIONS requests (preflight CORS)
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Tất cả các request khác sẽ được xử lý bởi JWT filter và routing
                        .anyExchange().permitAll()
                )
                .build();
    }

    /**
     * Cấu hình CORS cho Gateway
     * Xử lý tất cả CORS requests từ website, các service không cần cấu hình CORS riêng
     * 
     * Lưu ý: 
     * - JWT token được gửi qua Authorization header, không cần allowCredentials(true)
     * - Nếu website cần gửi cookies, hãy thay đổi allowedOriginPatterns thành danh sách cụ thể
     *   và set allowCredentials(true)
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Cho phép tất cả origins (dùng pattern để linh hoạt)
        // Nếu cần credentials (cookies), thay bằng danh sách cụ thể:
        // Arrays.asList("http://localhost:3000", "http://localhost:3001", "https://yourdomain.com")
        config.setAllowedOriginPatterns(Arrays.asList("*"));

        // Cho phép tất cả HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));
        
        // Cho phép tất cả headers (bao gồm Authorization, Content-Type, etc.)
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Expose headers mà client có thể đọc được
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Request-Id",
                "X-Trace-Id"
        ));
        
        // Không cần credentials vì JWT được gửi qua Authorization header
        // Nếu website cần gửi cookies, set true và thay allowedOriginPatterns thành danh sách cụ thể
        config.setAllowCredentials(false);
        
        // Cache preflight requests trong 1 giờ
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

    /**
     * Cấu hình CORS cho Spring Security
     * Đảm bảo Spring Security sử dụng cùng cấu hình CORS
     */
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Request-Id",
                "X-Trace-Id"
        ));
        // Không cần credentials vì JWT được gửi qua Authorization header
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

