package com.microdevice.messages.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microdevice.messages.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;

/**
 * Core JWT validation service that supports both RS256 (supervisor-issued) and
 * HS256 (shared-secret) tokens.
 *
 * <p>On startup, this service attempts to fetch the supervisor's JWKS. If the
 * supervisor is unreachable, the service enters fallback mode and only accepts
 * shared-secret tokens. The JWKS is periodically refreshed via a
 * {@link Scheduled @Scheduled} method.</p>
 *
 * <p>Token validation is routed based on the {@code iss} claim extracted from the
 * JWT payload before signature verification.</p>
 */
@Service
public class JwtValidationService {

    /** Logger for this service. */
    private static final Logger log = LoggerFactory.getLogger(JwtValidationService.class);

    /** JWT configuration properties (supervisor and shared-secret settings). */
    private final JwtConfig jwtConfig;

    /** HMAC secret key derived from the configured Base64-encoded shared secret. */
    private final SecretKey sharedSecretKey;

    /** Jackson ObjectMapper instantiated directly (not injected; see Spring Boot 4 note). */
    private final ObjectMapper objectMapper;

    /** HTTP client used to fetch the supervisor's JWKS endpoint. */
    private final HttpClient httpClient;

    /** RSA public key fetched from the supervisor's JWKS. May be {@code null} if unavailable. */
    private volatile RSAPublicKey supervisorPublicKey;

    /** Indicates whether the supervisor auth server is currently reachable. */
    private volatile boolean supervisorAvailable;

    /**
     * Constructs the service, initializes the shared-secret key, and attempts to
     * fetch the supervisor's JWKS. If the supervisor is unreachable, fallback mode
     * is activated.
     *
     * @param jwtConfig the JWT configuration properties
     */
    public JwtValidationService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

        this.sharedSecretKey = Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(jwtConfig.getSharedSecret().getKey())
        );

        try {
            fetchSupervisorPublicKey();
            log.info("Successfully fetched supervisor JWKS on startup");
        } catch (Exception e) {
            log.warn("Supervisor is unavailable on startup. Fallback mode active (shared-secret tokens only). Error: {}", e.getMessage());
            this.supervisorAvailable = false;
        }
    }

    /**
     * Fetches the RSA public key from the supervisor's JWKS endpoint and updates
     * the cached key and availability flag.
     *
     * @throws Exception if the JWKS endpoint is unreachable or returns invalid data
     */
    private void fetchSupervisorPublicKey() throws Exception {
        String jwksUrl = jwtConfig.getSupervisor().getJwksUrl();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(jwksUrl))
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("JWKS endpoint returned status " + response.statusCode());
        }

        JsonNode jwks = objectMapper.readTree(response.body());
        JsonNode keys = jwks.get("keys");

        if (keys == null || keys.isEmpty()) {
            throw new RuntimeException("No keys found in JWKS response");
        }

        JsonNode firstKey = keys.get(0);
        String n = firstKey.get("n").asText();
        String e = firstKey.get("e").asText();

        Base64.Decoder urlDecoder = Base64.getUrlDecoder();
        BigInteger modulus = new BigInteger(1, urlDecoder.decode(n));
        BigInteger exponent = new BigInteger(1, urlDecoder.decode(e));

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.supervisorPublicKey = (RSAPublicKey) keyFactory.generatePublic(spec);
        this.supervisorAvailable = true;
    }

    /**
     * Periodically refreshes the supervisor's RSA public key from the JWKS endpoint.
     *
     * <p>The interval is configured via {@code jwt.supervisor.jwks-refresh-interval}.
     * If the supervisor becomes available after being down, an informational log is emitted.
     * If the refresh fails while a cached key exists, the cached key remains in use.</p>
     */
    @Scheduled(fixedDelayString = "${jwt.supervisor.jwks-refresh-interval}")
    public void refreshSupervisorKey() {
        try {
            fetchSupervisorPublicKey();
            if (!supervisorAvailable) {
                log.info("Supervisor JWKS refreshed successfully - supervisor tokens now accepted");
            }
        } catch (Exception e) {
            if (supervisorAvailable) {
                log.warn("Failed to refresh supervisor JWKS. Cached key still in use. Error: {}", e.getMessage());
            } else {
                log.debug("Supervisor still unavailable: {}", e.getMessage());
            }
        }
    }

    /**
     * Validates a JWT token by routing to the appropriate validation method based on
     * the {@code iss} (issuer) claim.
     *
     * @param token the raw JWT string (without the "Bearer " prefix)
     * @return the validated claims from the token
     * @throws RuntimeException if the issuer is unknown or validation fails
     */
    public Claims validateToken(String token) {
        String issuer = extractIssuerWithoutValidation(token);

        if (jwtConfig.getSupervisor().getIssuer().equals(issuer)) {
            return validateSupervisorToken(token);
        } else if (jwtConfig.getSharedSecret().getIssuer().equals(issuer)) {
            return validateSharedSecretToken(token);
        } else {
            throw new RuntimeException("Unknown token issuer: " + issuer);
        }
    }

    /**
     * Validates an RS256-signed token issued by the supervisor auth server.
     *
     * @param token the raw JWT string
     * @return the validated claims
     * @throws RuntimeException if the supervisor public key is unavailable or validation fails
     */
    private Claims validateSupervisorToken(String token) {
        if (supervisorPublicKey == null) {
            throw new RuntimeException("Supervisor is unavailable, cannot validate supervisor-issued tokens");
        }

        return Jwts.parser()
            .verifyWith(supervisorPublicKey)
            .requireIssuer(jwtConfig.getSupervisor().getIssuer())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Validates an HS256-signed token using the shared secret key.
     *
     * @param token the raw JWT string
     * @return the validated claims
     * @throws RuntimeException if validation fails
     */
    private Claims validateSharedSecretToken(String token) {
        return Jwts.parser()
            .verifyWith(sharedSecretKey)
            .requireIssuer(jwtConfig.getSharedSecret().getIssuer())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Extracts the {@code iss} claim from a JWT without verifying its signature.
     *
     * <p>This is used to determine which validation strategy to apply before
     * performing full signature verification.</p>
     *
     * @param token the raw JWT string
     * @return the issuer string from the token payload
     * @throws RuntimeException if the token format is invalid or the issuer claim is missing
     */
    private String extractIssuerWithoutValidation(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                throw new RuntimeException("Invalid JWT format");
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode claims = objectMapper.readTree(payload);
            JsonNode issNode = claims.get("iss");
            if (issNode == null) {
                throw new RuntimeException("Token missing issuer claim");
            }
            return issNode.asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract issuer from token", e);
        }
    }

    /**
     * Returns whether the supervisor auth server is currently reachable.
     *
     * @return {@code true} if the supervisor's JWKS was successfully fetched, {@code false} otherwise
     */
    public boolean isSupervisorAvailable() {
        return supervisorAvailable;
    }
}
