package com.microdevice.messages.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the Messages resource server.
 *
 * <p>Allows cross-origin requests from the frontend application running at
 * {@code http://localhost:5173}.</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Registers CORS mappings for all endpoints, permitting standard HTTP methods,
     * all headers, and credentials from the frontend origin.
     *
     * @param registry the CORS registry to configure
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
