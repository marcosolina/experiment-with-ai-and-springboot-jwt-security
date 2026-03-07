package com.microdevice.supervisor.dto;

public record LoginResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
}
