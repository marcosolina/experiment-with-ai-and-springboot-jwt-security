package com.microdevice.supervisor.dto;

/**
 * Request payload for new user registration.
 *
 * @param username the desired username for the new account
 * @param password the plain-text password (will be BCrypt-hashed before storage)
 */
public record RegisterRequest(String username, String password) {
}
