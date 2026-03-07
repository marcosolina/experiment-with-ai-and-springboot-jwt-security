package com.microdevice.messages.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for JWT validation, bound to the {@code jwt.*} prefix
 * in {@code application.yml}.
 *
 * <p>Contains nested configuration for the supervisor auth server (RS256 tokens)
 * and the shared-secret fallback (HS256 tokens).</p>
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /** Configuration for the supervisor auth server (RS256). */
    private Supervisor supervisor = new Supervisor();

    /** Configuration for the shared-secret token validation (HS256). */
    private SharedSecret sharedSecret = new SharedSecret();

    /**
     * Returns the supervisor configuration.
     *
     * @return the supervisor JWT settings
     */
    public Supervisor getSupervisor() {
        return supervisor;
    }

    /**
     * Sets the supervisor configuration.
     *
     * @param supervisor the supervisor JWT settings
     */
    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    /**
     * Returns the shared-secret configuration.
     *
     * @return the shared-secret JWT settings
     */
    public SharedSecret getSharedSecret() {
        return sharedSecret;
    }

    /**
     * Sets the shared-secret configuration.
     *
     * @param sharedSecret the shared-secret JWT settings
     */
    public void setSharedSecret(SharedSecret sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    /**
     * Nested configuration for the supervisor auth server.
     *
     * <p>Holds the JWKS endpoint URL, the expected issuer claim, and the
     * interval (in milliseconds) at which the JWKS is refreshed.</p>
     */
    public static class Supervisor {
        /** URL of the supervisor's JWKS endpoint. */
        private String jwksUrl;

        /** Expected {@code iss} claim value for supervisor-issued tokens. */
        private String issuer;

        /** Interval in milliseconds between JWKS refresh attempts. */
        private long jwksRefreshInterval;

        /**
         * Returns the JWKS endpoint URL.
         *
         * @return the JWKS URL
         */
        public String getJwksUrl() {
            return jwksUrl;
        }

        /**
         * Sets the JWKS endpoint URL.
         *
         * @param jwksUrl the JWKS URL
         */
        public void setJwksUrl(String jwksUrl) {
            this.jwksUrl = jwksUrl;
        }

        /**
         * Returns the expected issuer for supervisor tokens.
         *
         * @return the issuer string
         */
        public String getIssuer() {
            return issuer;
        }

        /**
         * Sets the expected issuer for supervisor tokens.
         *
         * @param issuer the issuer string
         */
        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        /**
         * Returns the JWKS refresh interval in milliseconds.
         *
         * @return the refresh interval
         */
        public long getJwksRefreshInterval() {
            return jwksRefreshInterval;
        }

        /**
         * Sets the JWKS refresh interval in milliseconds.
         *
         * @param jwksRefreshInterval the refresh interval
         */
        public void setJwksRefreshInterval(long jwksRefreshInterval) {
            this.jwksRefreshInterval = jwksRefreshInterval;
        }
    }

    /**
     * Nested configuration for shared-secret (HS256) token validation.
     *
     * <p>Holds the Base64-encoded HMAC key and the expected issuer claim.</p>
     */
    public static class SharedSecret {
        /** Base64-encoded HMAC shared secret key. */
        private String key;

        /** Expected {@code iss} claim value for shared-secret tokens. */
        private String issuer;

        /**
         * Returns the Base64-encoded shared secret key.
         *
         * @return the key string
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets the Base64-encoded shared secret key.
         *
         * @param key the key string
         */
        public void setKey(String key) {
            this.key = key;
        }

        /**
         * Returns the expected issuer for shared-secret tokens.
         *
         * @return the issuer string
         */
        public String getIssuer() {
            return issuer;
        }

        /**
         * Sets the expected issuer for shared-secret tokens.
         *
         * @param issuer the issuer string
         */
        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
}
