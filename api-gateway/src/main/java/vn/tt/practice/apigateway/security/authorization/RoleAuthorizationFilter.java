package vn.tt.practice.apigateway.security.authorization;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import vn.tt.practice.apigateway.exception.UnauthorizedException;
import vn.tt.practice.apigateway.security.jwt.JwtTokenValidator;

import java.util.List;

@Component
public class RoleAuthorizationFilter extends AbstractGatewayFilterFactory<RoleAuthorizationFilter.Config> {

    private final JwtTokenValidator jwtTokenValidator;

    public RoleAuthorizationFilter(JwtTokenValidator jwtTokenValidator) {
        super(Config.class);
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            try {
                Claims claims = jwtTokenValidator.validate(token);
                List<String> roles = claims.get("roles", List.class);

                if (roles == null || !roles.contains(config.getRole())) {
                    throw new UnauthorizedException("Insufficient permissions");
                }
            } catch (Exception e) {
                throw new UnauthorizedException("Invalid token");
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
        private String role;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
