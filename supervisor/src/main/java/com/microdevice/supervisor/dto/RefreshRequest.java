package com.microdevice.supervisor.dto;

/**
 * Request payload for refreshing an access token.
 *
 * @param refreshToken the current valid refresh token to exchange for a new token pair
 */
public record RefreshRequest(String refreshToken) {
}
