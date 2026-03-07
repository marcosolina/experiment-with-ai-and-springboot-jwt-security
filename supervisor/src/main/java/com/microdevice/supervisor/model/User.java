package com.microdevice.supervisor.model;

import java.util.List;

/**
 * Immutable domain model representing a user in the system.
 *
 * <p>Each user has a unique identifier, a username, a BCrypt-hashed password,
 * and a list of roles that determine access permissions.</p>
 */
public class User {

    /** Unique identifier for the user (UUID). */
    private final String id;

    /** The user's login name. */
    private final String username;

    /** The user's BCrypt-hashed password. */
    private final String password;

    /** The roles assigned to this user (e.g., "ADMIN", "USER"). */
    private final List<String> roles;

    /**
     * Constructs a new User with the specified attributes.
     *
     * @param id       the unique user identifier
     * @param username the login username
     * @param password the BCrypt-hashed password
     * @param roles    the list of roles assigned to the user
     */
    public User(String id, String username, String password, List<String> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    /**
     * Returns the unique user identifier.
     *
     * @return the user ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the username.
     *
     * @return the login username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the BCrypt-hashed password.
     *
     * @return the hashed password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the list of roles assigned to the user.
     *
     * @return the user's roles
     */
    public List<String> getRoles() {
        return roles;
    }
}
