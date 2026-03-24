package vn.tt.practice.productservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLES_HEADER = "X-User-Roles";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .version("1.0.0")
                        .description("Product catalog management: products, categories, images, specs. " +
                                "This service trusts Gateway headers and requires X-Internal-Token.")
                        .contact(new Contact().name("E-Commerce Team").email("dev@ecommerce.local")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Local (direct)"),
                        new Server().url("http://localhost:8181").description("Via API Gateway")
                ))
                .components(new Components()
                        // Optional: show Authorization header in Swagger (for gateway calls)
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer commonHeadersCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    operation.addParametersItem(new io.swagger.v3.oas.models.parameters.Parameter()
                            .in("header")
                            .name(INTERNAL_TOKEN_HEADER)
                            .required(true)
                            .description("Internal token injected by Gateway. Required to prevent spoofing.")
                            .schema(new io.swagger.v3.oas.models.media.StringSchema()));

                    operation.addParametersItem(new io.swagger.v3.oas.models.parameters.Parameter()
                            .in("header")
                            .name(CORRELATION_ID_HEADER)
                            .required(false)
                            .description("Correlation id. If missing, Gateway generates.")
                            .schema(new io.swagger.v3.oas.models.media.StringSchema()));

                    operation.addParametersItem(new io.swagger.v3.oas.models.parameters.Parameter()
                            .in("header")
                            .name(USER_ID_HEADER)
                            .required(false)
                            .description("User id injected by Gateway after JWT validation.")
                            .schema(new io.swagger.v3.oas.models.media.StringSchema()));

                    operation.addParametersItem(new io.swagger.v3.oas.models.parameters.Parameter()
                            .in("header")
                            .name(USER_ROLES_HEADER)
                            .required(false)
                            .description("Comma-separated roles injected by Gateway: ROLE_USER,ROLE_ADMIN,...")
                            .schema(new io.swagger.v3.oas.models.media.StringSchema()));
                })
        );
    }
}
