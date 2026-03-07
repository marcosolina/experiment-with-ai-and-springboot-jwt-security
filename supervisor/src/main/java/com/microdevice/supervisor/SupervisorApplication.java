package com.microdevice.supervisor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Supervisor authorization server.
 *
 * <p>This Spring Boot application acts as an authentication and authorization server
 * that issues RS256 JWT tokens and manages user accounts.</p>
 */
@SpringBootApplication
public class SupervisorApplication {

    /**
     * Launches the Supervisor application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(SupervisorApplication.class, args);
    }
}
