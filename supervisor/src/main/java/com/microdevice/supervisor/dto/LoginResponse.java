package com.microdevice.supervisor.dto;

/**
 * Response payload returned after successful authentication.
 *
 * @param accessToken  the short-lived JWT access token
 * @param refreshToken the long-lived JWT refresh token for obtaining new access tokens
 * @param tokenType    the token type (always "Bearer")
 * @param expiresIn    the access token lifetime in seconds
 */
public record LoginResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
}
