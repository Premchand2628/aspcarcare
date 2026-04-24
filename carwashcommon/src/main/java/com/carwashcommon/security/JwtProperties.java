package com.carwashcommon.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenMinutes = 20;
    private long refreshTokenDays = 7;

    /**
     * Expected JWT issuer. When set, only tokens with a matching "iss" claim
     * are accepted by JwtAuthenticationFilter. Tokens minted by this service
     * always carry this claim so a compromised secondary service that somehow
     * forges an HS256 signature still fails the iss check unless it also
     * shares the same issuer configuration.
     * Optional — leave blank to skip validation.
     */
    private String issuer;

    /**
     * Signing algorithm used when MINTING tokens. Either "HS256" (shared
     * secret, legacy) or "RS256" (asymmetric). All services can always VERIFY
     * both algorithms as long as the matching key material is configured, so
     * rollout is: distribute publicKey everywhere → flip otploginauth to
     * RS256 → HS256 tokens continue to validate until they expire.
     */
    private String algorithm = "HS256";

    /**
     * RSA private key in PEM format (PKCS#8), required when algorithm=RS256.
     * Only the token-minting service (otploginauth) needs this set. Supply via
     * environment variable, not a committed file.
     */
    private String privateKey;

    /**
     * RSA public key in PEM format (X.509 SubjectPublicKeyInfo), required by
     * every service that needs to verify RS256 tokens. Safe to distribute.
     */
    private String publicKey;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getAccessTokenMinutes() { return accessTokenMinutes; }
    public void setAccessTokenMinutes(long accessTokenMinutes) { this.accessTokenMinutes = accessTokenMinutes; }

    public long getRefreshTokenDays() { return refreshTokenDays; }
    public void setRefreshTokenDays(long refreshTokenDays) { this.refreshTokenDays = refreshTokenDays; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
}
//package com.carwashcommon.security;
//
//import org.springframework.boot.context.properties.ConfigurationProperties;
//
//@ConfigurationProperties(prefix = "jwt")
//public class JwtProperties {
//    /**
//     * HS256 secret. Use a long random string (>= 32 chars).
//     */
//    private String secret;
//
//    /**
//     * In minutes (optional).
//     */
//    private long accessTokenMinutes = 60;
//
//    public String getSecret() { return secret; }
//    public void setSecret(String secret) { this.secret = secret; }
//
//    public long getAccessTokenMinutes() { return accessTokenMinutes; }
//    public void setAccessTokenMinutes(long accessTokenMinutes) { this.accessTokenMinutes = accessTokenMinutes; }
//}
