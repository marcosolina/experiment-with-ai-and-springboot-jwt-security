package com.microdevice.messages;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Messages resource server application.
 *
 * <p>Enables scheduling to support periodic JWKS refresh from the supervisor auth server.</p>
 */
@SpringBootApplication
@EnableScheduling
public class MessagesApplication {

    /**
     * Launches the Messages Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(MessagesApplication.class, args);
    }
}
