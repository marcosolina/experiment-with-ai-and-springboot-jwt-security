package com.microdevice.supervisor.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) configuration for the Supervisor API.
 *
 * <p>Configures the Swagger UI with API metadata and a Bearer JWT authentication
 * scheme, allowing developers to test authenticated endpoints directly from the documentation.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates the OpenAPI specification bean with API info and JWT Bearer security scheme.
     *
     * @return the configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Supervisor API")
                .description("Authorization Server - Issues RS256 JWT tokens and manages users")
                .version("1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"))
            .components(new Components()
                .addSecuritySchemes("Bearer", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
