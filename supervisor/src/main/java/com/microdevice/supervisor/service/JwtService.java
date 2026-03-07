package com.microdevice.supervisor.service;

import com.microdevice.supervisor.config.RsaKeyConfig;
import com.microdevice.supervisor.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for JWT token generation, validation, and lifecycle management.
 *
 * <p>Generates RS256-signed access tokens (short-lived) and refresh tokens (long-lived).
 * Active refresh tokens are tracked in a {@link ConcurrentHashMap} to support revocation
 * and token rotation.</p>
 */
@Service
public class JwtService {

    /** RSA key configuration used for signing and verifying tokens. */
    private final RsaKeyConfig rsaKeyConfig;

    /** Access token expiration time in milliseconds. */
    private final long accessTokenExpiration;

    /** Refresh token expiration time in milliseconds. */
    private final long refreshTokenExpiration;

    /** Map of active refresh tokens to their associated usernames for revocation tracking. */
    private final Map<String, String> activeRefreshTokens = new ConcurrentHashMap<>();

    /**
     * Constructs the JWT service with RSA key configuration and token expiration settings.
     *
     * @param rsaKeyConfig           the RSA key pair configuration
     * @param accessTokenExpiration  access token lifetime in milliseconds
     * @param refreshTokenExpiration refresh token lifetime in milliseconds
     */
    public JwtService(
            RsaKeyConfig rsaKeyConfig,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.rsaKeyConfig = rsaKeyConfig;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Generates a short-lived RS256-signed access token for the given user.
     *
     * <p>The token includes the user's username as the subject, their roles as a custom claim,
     * and the supervisor's key ID in the header.</p>
     *
     * @param user the user for whom to generate the access token
     * @return the compact JWT access token string
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
            .header().keyId(rsaKeyConfig.getKid()).and()
            .issuer("supervisor")
            .subject(user.getUsername())
            .claim("roles", user.getRoles())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(rsaKeyConfig.getPrivateKey(), Jwts.SIG.RS256)
            .compact();
    }

    /**
     * Generates a long-lived RS256-signed refresh token for the given user.
     *
     * <p>The token includes a {@code type=refresh} claim to distinguish it from access tokens.
     * The generated token is registered in the active refresh tokens map for revocation tracking.</p>
     *
     * @param user the user for whom to generate the refresh token
     * @return the compact JWT refresh token string
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        String token = Jwts.builder()
            .header().keyId(rsaKeyConfig.getKid()).and()
            .issuer("supervisor")
            .subject(user.getUsername())
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(expiry)
            .signWith(rsaKeyConfig.getPrivateKey(), Jwts.SIG.RS256)
            .compact();

        activeRefreshTokens.put(token, user.getUsername());
        return token;
    }

    /**
     * Validates a JWT token by verifying its RS256 signature and issuer claim.
     *
     * @param token the JWT token string to validate
     * @return the parsed {@link Claims} from the token payload
     * @throws io.jsonwebtoken.JwtException if the token is invalid, expired, or has a bad signature
     */
    public Claims validateToken(String token) {
        return Jwts.parser()
            .verifyWith(rsaKeyConfig.getPublicKey())
            .requireIssuer("supervisor")
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Extracts the username (subject) from a validated JWT token.
     *
     * @param token the JWT token string
     * @return the username stored in the token's subject claim
     * @throws io.jsonwebtoken.JwtException if the token is invalid
     */
    public String getUsernameFromToken(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Checks whether a refresh token is valid and currently active.
     *
     * <p>A refresh token is considered valid if it exists in the active tokens map,
     * its signature and expiration are valid, and it contains a {@code type=refresh} claim.
     * Invalid tokens are automatically removed from the active tokens map.</p>
     *
     * @param refreshToken the refresh token to validate
     * @return {@code true} if the refresh token is valid and active, {@code false} otherwise
     */
    public boolean isRefreshTokenValid(String refreshToken) {
        if (!activeRefreshTokens.containsKey(refreshToken)) {
            return false;
        }
        try {
            Claims claims = validateToken(refreshToken);
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            activeRefreshTokens.remove(refreshToken);
            return false;
        }
    }

    /**
     * Revokes a refresh token by removing it from the active tokens map.
     *
     * @param refreshToken the refresh token to revoke
     */
    public void revokeRefreshToken(String refreshToken) {
        activeRefreshTokens.remove(refreshToken);
    }

    /**
     * Returns the access token expiration time in seconds.
     *
     * @return the access token lifetime in seconds
     */
    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }
}
