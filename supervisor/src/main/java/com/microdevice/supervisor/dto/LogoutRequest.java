package com.microdevice.supervisor.dto;

/**
 * Request payload for user logout.
 *
 * @param refreshToken the refresh token to revoke upon logout
 */
public record LogoutRequest(String refreshToken) {
}
