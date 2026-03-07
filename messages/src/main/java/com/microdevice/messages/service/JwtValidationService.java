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

@Service
public class JwtValidationService {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationService.class);

    private final JwtConfig jwtConfig;
    private final SecretKey sharedSecretKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    private volatile RSAPublicKey supervisorPublicKey;
    private volatile boolean supervisorAvailable;

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

    private Claims validateSharedSecretToken(String token) {
        return Jwts.parser()
            .verifyWith(sharedSecretKey)
            .requireIssuer(jwtConfig.getSharedSecret().getIssuer())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

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

    public boolean isSupervisorAvailable() {
        return supervisorAvailable;
    }
}
