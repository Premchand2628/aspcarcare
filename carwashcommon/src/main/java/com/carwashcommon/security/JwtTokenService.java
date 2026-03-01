package com.carwashcommon.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

public class JwtTokenService {

    private final JwtProperties props;

    public JwtTokenService(JwtProperties props) {
        this.props = props;
    }

    private Key signingKey() {
        if (props.getSecret() == null || props.getSecret().trim().length() < 32) {
            throw new IllegalStateException("jwt.secret must be set and at least 32 characters");
        }
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String subjectPhone, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getAccessTokenMinutes(), ChronoUnit.MINUTES);

        JwtBuilder b = Jwts.builder()
                .setSubject(subjectPhone)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(signingKey(), SignatureAlgorithm.HS256);

        if (claims != null && !claims.isEmpty()) {
            b.addClaims(claims);
        }

        return b.compact();
    }

    public Jws<Claims> parseAndValidate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token);
    }

    public String getSubject(String token) {
        return parseAndValidate(token).getBody().getSubject();
    }
}
