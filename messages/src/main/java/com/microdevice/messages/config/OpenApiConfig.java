package com.microdevice.messages.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration for the Messages API.
 *
 * <p>Configures API metadata and a global Bearer JWT security scheme so that
 * Swagger UI provides an "Authorize" button for entering JWT tokens.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates the {@link OpenAPI} specification bean with API info and a Bearer JWT
     * security scheme.
     *
     * @return the configured OpenAPI definition
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Messages API")
                .description("Resource Server - Validates both RS256 (supervisor) and HS256 (shared-secret) JWT tokens")
                .version("1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"))
            .components(new Components()
                .addSecuritySchemes("Bearer", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
