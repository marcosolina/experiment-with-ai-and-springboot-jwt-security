package com.microdevice.supervisor.config;

import org.springframework.context.annotation.Configuration;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Configuration class that generates and holds the RSA key pair used for signing JWTs.
 *
 * <p>A 2048-bit RSA key pair is generated at application startup. The private key is
 * used to sign tokens, and the public key is exposed via the JWKS endpoint so that
 * resource servers can verify token signatures.</p>
 */
@Configuration
public class RsaKeyConfig {

    /** Key ID (kid) included in JWT headers to identify the signing key. */
    private static final String KID = "supervisor-key-1";

    /** RSA public key used for token signature verification. */
    private final RSAPublicKey publicKey;

    /** RSA private key used for signing JWT tokens. */
    private final RSAPrivateKey privateKey;

    /**
     * Generates a new 2048-bit RSA key pair at construction time.
     *
     * @throws NoSuchAlgorithmException if the RSA algorithm is not available
     */
    public RsaKeyConfig() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    /**
     * Returns the RSA public key.
     *
     * @return the RSA public key
     */
    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Returns the RSA private key.
     *
     * @return the RSA private key
     */
    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Returns the key ID (kid) used in JWT headers.
     *
     * @return the key identifier string
     */
    public String getKid() {
        return KID;
    }
}
