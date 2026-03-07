package com.microdevice.supervisor.service;

import com.microdevice.supervisor.model.User;
import com.microdevice.supervisor.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for user management, including authentication and registration.
 *
 * <p>Delegates persistence to {@link UserRepository} and uses a {@link PasswordEncoder}
 * for BCrypt password hashing and verification.</p>
 */
@Service
public class UserService {

    /** Repository for user persistence operations. */
    private final UserRepository userRepository;

    /** Encoder used for hashing and verifying passwords. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs the user service with required dependencies.
     *
     * @param userRepository  the user repository
     * @param passwordEncoder the password encoder
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticates a user by verifying username and password.
     *
     * @param username the username to authenticate
     * @param password the raw password to verify
     * @return the authenticated {@link User}
     * @throws RuntimeException if the username is not found or the password does not match
     */
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    /**
     * Registers a new user with the USER role.
     *
     * @param username the desired username
     * @param password the raw password (will be BCrypt-hashed before storage)
     * @return the newly created {@link User}
     * @throws RuntimeException if the username is already taken
     */
    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User(
            UUID.randomUUID().toString(),
            username,
            passwordEncoder.encode(password),
            List.of("USER")
        );

        return userRepository.save(user);
    }

    /**
     * Looks up a user by username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Returns all registered users.
     *
     * @return a list of all users
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }
}
