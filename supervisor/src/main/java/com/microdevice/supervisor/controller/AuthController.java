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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final RsaKeyConfig rsaKeyConfig;

    public AuthController(UserService userService, JwtService jwtService, RsaKeyConfig rsaKeyConfig) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.rsaKeyConfig = rsaKeyConfig;
    }

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        jwtService.revokeRefreshToken(request.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

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
