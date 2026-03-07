package com.microdevice.supervisor.controller;

import com.microdevice.supervisor.config.RsaKeyConfig;
import com.microdevice.supervisor.dto.*;
import com.microdevice.supervisor.model.User;
import com.microdevice.supervisor.service.JwtService;
import com.microdevice.supervisor.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * REST controller handling authentication operations.
 *
 * <p>Provides endpoints for user login, registration, token refresh, logout,
 * and the JWKS (JSON Web Key Set) endpoint for public key distribution to resource servers.</p>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** Service for user authentication and registration. */
    private final UserService userService;

    /** Service for JWT token generation and validation. */
    private final JwtService jwtService;

    /** RSA key configuration for the JWKS endpoint. */
    private final RsaKeyConfig rsaKeyConfig;

    /**
     * Constructs the authentication controller with required dependencies.
     *
     * @param userService  the user service for authentication and registration
     * @param jwtService   the JWT service for token operations
     * @param rsaKeyConfig the RSA key configuration for JWKS
     */
    public AuthController(UserService userService, JwtService jwtService, RsaKeyConfig rsaKeyConfig) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.rsaKeyConfig = rsaKeyConfig;
    }

    /**
     * Authenticates a user and issues access and refresh tokens.
     *
     * @param request the login request containing username and password
     * @return a {@link LoginResponse} with tokens on success, or a 401 error on failure
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.authenticate(request.username(), request.password());
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return ResponseEntity.ok(new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Registers a new user account with the USER role.
     *
     * @param request the registration request containing username and password
     * @return a success message with the username, or a 400 error if registration fails
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request.username(), request.password());
            return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "username", user.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * <p>The old refresh token is revoked and a new token pair (access + refresh) is issued.
     * This implements refresh token rotation for improved security.</p>
     *
     * @param request the refresh request containing the current refresh token
     * @return a new {@link LoginResponse} with rotated tokens, or a 401 error if the refresh token is invalid
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid refresh token"));
        }

        String username = jwtService.getUsernameFromToken(refreshToken);
        User user = userService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        jwtService.revokeRefreshToken(refreshToken);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(new LoginResponse(
            newAccessToken,
            newRefreshToken,
            "Bearer",
            jwtService.getAccessTokenExpirationSeconds()
        ));
    }

    /**
     * Logs out a user by revoking their refresh token.
     *
     * @param request the logout request containing the refresh token to revoke
     * @return a success message confirming logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        jwtService.revokeRefreshToken(request.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Exposes the JSON Web Key Set (JWKS) containing the server's RSA public key.
     *
     * <p>Resource servers use this endpoint to retrieve the public key for verifying
     * RS256-signed JWT tokens issued by this authorization server.</p>
     *
     * @return the JWKS response containing the RSA public key in JWK format
     */
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<?> jwks() {
        RSAPublicKey publicKey = rsaKeyConfig.getPublicKey();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        Map<String, Object> jwk = Map.of(
            "kty", "RSA",
            "kid", rsaKeyConfig.getKid(),
            "use", "sig",
            "alg", "RS256",
            "n", encoder.encodeToString(publicKey.getModulus().toByteArray()),
            "e", encoder.encodeToString(publicKey.getPublicExponent().toByteArray())
        );

        return ResponseEntity.ok(Map.of("keys", List.of(jwk)));
    }
}
