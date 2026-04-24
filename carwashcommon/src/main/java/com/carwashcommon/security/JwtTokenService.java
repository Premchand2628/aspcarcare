package com.carwashcommon.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * Issues and validates JWTs. Supports both HS256 (shared secret, legacy) and
 * RS256 (asymmetric). Verification accepts whichever algorithm the token
 * declares in its header, so rollout is non-breaking:
 *
 *   1. Deploy everyone with publicKey + secret configured, algorithm=HS256.
 *   2. Flip otploginauth to algorithm=RS256. New tokens are RS256, in-flight
 *      HS256 tokens continue to validate until they expire.
 *   3. Later, remove secret from non-issuing services.
 */
public class JwtTokenService {

    private final JwtProperties props;
    private volatile Key cachedHmacKey;
    private volatile PrivateKey cachedPrivateKey;
    private volatile PublicKey cachedPublicKey;

    public JwtTokenService(JwtProperties props) {
        this.props = props;
    }

    // ---------- key material ----------

    private Key hmacKey() {
        Key k = cachedHmacKey;
        if (k != null) return k;
        if (props.getSecret() == null || props.getSecret().trim().length() < 32) {
            throw new IllegalStateException("jwt.secret must be set and at least 32 characters");
        }
        k = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
        cachedHmacKey = k;
        return k;
    }

    private PrivateKey rsaPrivateKey() {
        PrivateKey k = cachedPrivateKey;
        if (k != null) return k;
        String pem = props.getPrivateKey();
        if (pem == null || pem.isBlank()) {
            throw new IllegalStateException("jwt.private-key must be set when algorithm=RS256");
        }
        try {
            byte[] der = pemToDer(pem, "PRIVATE KEY");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            k = kf.generatePrivate(new PKCS8EncodedKeySpec(der));
            cachedPrivateKey = k;
            return k;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse jwt.private-key (expected PKCS#8 PEM)", e);
        }
    }

    private PublicKey rsaPublicKey() {
        PublicKey k = cachedPublicKey;
        if (k != null) return k;
        String pem = props.getPublicKey();
        if (pem == null || pem.isBlank()) {
            throw new IllegalStateException("jwt.public-key must be set to verify RS256 tokens");
        }
        try {
            byte[] der = pemToDer(pem, "PUBLIC KEY");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            k = kf.generatePublic(new X509EncodedKeySpec(der));
            cachedPublicKey = k;
            return k;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse jwt.public-key (expected X.509 SubjectPublicKeyInfo PEM)", e);
        }
    }

    private static byte[] pemToDer(String pem, String expectedLabel) {
        String body = pem.replace("\r", "")
                .replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s+", "");
        return Base64.getDecoder().decode(body);
    }

    private boolean useRs256() {
        return "RS256".equalsIgnoreCase(props.getAlgorithm());
    }

    // ---------- sign ----------

    public String generateAccessToken(String subjectPhone, Map<String, Object> claims) {
        return build(subjectPhone, claims, "access",
                Instant.now().plus(props.getAccessTokenMinutes(), ChronoUnit.MINUTES));
    }

    public String generateRefreshToken(String subjectPhone, Map<String, Object> claims) {
        return build(subjectPhone, claims, "refresh",
                Instant.now().plus(props.getRefreshTokenDays(), ChronoUnit.DAYS));
    }

    private String build(String subject, Map<String, Object> claims, String type, Instant exp) {
        Instant now = Instant.now();

        JwtBuilder b = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("type", type);

        if (useRs256()) {
            b.signWith(rsaPrivateKey(), SignatureAlgorithm.RS256);
        } else {
            b.signWith(hmacKey(), SignatureAlgorithm.HS256);
        }

        if (props.getIssuer() != null && !props.getIssuer().isBlank()) {
            b.setIssuer(props.getIssuer().trim());
        }

        if (claims != null && !claims.isEmpty()) {
            b.addClaims(claims);
        }

        return b.compact();
    }

    // ---------- verify ----------

    public Jws<Claims> parseAndValidate(String token) {
        SigningKeyResolverAdapter resolver = new SigningKeyResolverAdapter() {
            @Override
            public Key resolveSigningKey(JwsHeader header, Claims claims) {
                String alg = header.getAlgorithm();
                if (alg == null) {
                    throw new SignatureException("Missing JWT alg header");
                }
                if (alg.startsWith("RS")) {
                    return rsaPublicKey();
                }
                if (alg.startsWith("HS")) {
                    return hmacKey();
                }
                throw new SignatureException("Unsupported JWT alg: " + alg);
            }
        };

        JwtParserBuilder parserBuilder = Jwts.parserBuilder()
                .setSigningKeyResolver(resolver);

        if (props.getIssuer() != null && !props.getIssuer().isBlank()) {
            parserBuilder.requireIssuer(props.getIssuer().trim());
        }

        return parserBuilder.build().parseClaimsJws(token);
    }

    public String getSubject(String token) {
        return parseAndValidate(token).getBody().getSubject();
    }
}
