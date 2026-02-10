package vn.tt.practice.cartservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cartServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cart Service API")
                        .version("1.0.0")
                        .description("Shopping cart management: add items, update quantities, clear cart. " +
                                "Fetches product details from Product Service via Feign.")
                        .contact(new Contact().name("E-Commerce Team").email("dev@ecommerce.local")))
                .servers(List.of(
                        new Server().url("http://localhost:8086").description("Local (direct)"),
                        new Server().url("http://localhost:8181").description("Via API Gateway")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
