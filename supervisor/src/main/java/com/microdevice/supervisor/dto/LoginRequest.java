package com.microdevice.supervisor.dto;

/**
 * Request payload for user login.
 *
 * @param username the user's login name
 * @param password the user's plain-text password
 */
public record LoginRequest(String username, String password) {
}
