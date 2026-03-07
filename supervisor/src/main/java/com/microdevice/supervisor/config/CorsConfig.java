package com.microdevice.supervisor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the Supervisor API.
 *
 * <p>Allows cross-origin requests from the frontend application running
 * at {@code http://localhost:5173} with standard HTTP methods and credentials.</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Registers CORS mappings that permit the frontend origin to access all API endpoints.
     *
     * @param registry the {@link CorsRegistry} to add mappings to
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
