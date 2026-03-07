package com.microdevice.messages.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private Supervisor supervisor = new Supervisor();
    private SharedSecret sharedSecret = new SharedSecret();

    public Supervisor getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    public SharedSecret getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(SharedSecret sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public static class Supervisor {
        private String jwksUrl;
        private String issuer;
        private long jwksRefreshInterval;

        public String getJwksUrl() {
            return jwksUrl;
        }

        public void setJwksUrl(String jwksUrl) {
            this.jwksUrl = jwksUrl;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public long getJwksRefreshInterval() {
            return jwksRefreshInterval;
        }

        public void setJwksRefreshInterval(long jwksRefreshInterval) {
            this.jwksRefreshInterval = jwksRefreshInterval;
        }
    }

    public static class SharedSecret {
        private String key;
        private String issuer;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
}
