package com.microdevice.supervisor.repository;

import com.microdevice.supervisor.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory user repository backed by a {@link ConcurrentHashMap}.
 *
 * <p>Pre-seeded with a default admin user (username: {@code admin}, password: {@code admin123})
 * that has both ADMIN and USER roles. Users are keyed by username for fast lookup.</p>
 */
@Repository
public class UserRepository {

    /** Thread-safe map storing users keyed by username. */
    private final Map<String, User> users = new ConcurrentHashMap<>();

    /**
     * Initializes the repository and seeds it with the default admin user.
     *
     * @param passwordEncoder the encoder used to hash the admin user's password
     */
    public UserRepository(PasswordEncoder passwordEncoder) {
        User admin = new User(
            UUID.randomUUID().toString(),
            "admin",
            passwordEncoder.encode("admin123"),
            List.of("ADMIN", "USER")
        );
        users.put(admin.getUsername(), admin);
    }

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    /**
     * Saves a user to the repository. If a user with the same username already exists, it is replaced.
     *
     * @param user the user to save
     * @return the saved user
     */
    public User save(User user) {
        users.put(user.getUsername(), user);
        return user;
    }

    /**
     * Returns all users in the repository.
     *
     * @return a list of all stored users
     */
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}
