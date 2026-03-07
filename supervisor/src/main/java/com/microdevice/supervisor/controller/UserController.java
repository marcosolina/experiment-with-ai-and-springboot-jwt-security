package com.microdevice.supervisor.controller;

import com.microdevice.supervisor.model.User;
import com.microdevice.supervisor.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for user-related operations.
 *
 * <p>Provides endpoints for retrieving the authenticated user's profile
 * and listing all users (restricted to administrators).</p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /** Service for user lookup operations. */
    private final UserService userService;

    /**
     * Constructs the user controller with the required user service.
     *
     * @param userService the user service
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns the profile of the currently authenticated user.
     *
     * @param authentication the current authentication context containing the user's identity
     * @return the user's id, username, and roles
     * @throws RuntimeException if the authenticated user is not found in the repository
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "roles", user.getRoles()
        ));
    }

    /**
     * Lists all registered users. Restricted to users with the ADMIN role.
     *
     * @return a list of user summaries containing id, username, and roles
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listUsers() {
        List<Map<String, Object>> users = userService.findAll().stream()
            .map(user -> Map.<String, Object>of(
                "id", user.getId(),
                "username", user.getUsername(),
                "roles", user.getRoles()
            ))
            .toList();

        return ResponseEntity.ok(users);
    }
}
