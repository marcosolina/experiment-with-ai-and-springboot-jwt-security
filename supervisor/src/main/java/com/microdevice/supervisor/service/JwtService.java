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

@Service
public class JwtService {

    private final RsaKeyConfig rsaKeyConfig;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final Map<String, String> activeRefreshTokens = new ConcurrentHashMap<>();

    public JwtService(
            RsaKeyConfig rsaKeyConfig,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.rsaKeyConfig = rsaKeyConfig;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

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

    public Claims validateToken(String token) {
        return Jwts.parser()
            .verifyWith(rsaKeyConfig.getPublicKey())
            .requireIssuer("supervisor")
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String getUsernameFromToken(String token) {
        return validateToken(token).getSubject();
    }

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

    public void revokeRefreshToken(String refreshToken) {
        activeRefreshTokens.remove(refreshToken);
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpiration / 1000;
    }
}
