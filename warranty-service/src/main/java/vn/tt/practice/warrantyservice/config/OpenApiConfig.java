package vn.tt.practice.warrantyservice.config;

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
    public OpenAPI warrantyServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Warranty Service API")
                        .version("1.0.0")
                        .description("Warranty management: create warranties, submit claims, track warranty status. " +
                                "Auto-creates warranty when order is paid.")
                        .contact(new Contact().name("E-Commerce Team").email("dev@ecommerce.local")))
                .servers(List.of(
                        new Server().url("http://localhost:8088").description("Local (direct)"),
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
