package com.carwashcommon.security;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory token blacklist for revoked JWTs.
 * Tokens are stored until their natural expiry, then auto-evicted.
 * For multi-instance deployments, replace with Redis-backed implementation.
 */
public class TokenBlacklistService {

    // tokenHash -> expiryEpochSeconds
    private final ConcurrentMap<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * Blacklist a token until its natural expiry.
     * @param token   the raw JWT string
     * @param expiryEpochSeconds  the token's exp claim as epoch seconds
     */
    public void blacklist(String token, long expiryEpochSeconds) {
        evictExpired();
        blacklist.put(hashToken(token), expiryEpochSeconds);
    }

    /**
     * Check if a token has been blacklisted.
     */
    public boolean isBlacklisted(String token) {
        Long expiry = blacklist.get(hashToken(token));
        if (expiry == null) return false;
        if (Instant.now().getEpochSecond() > expiry) {
            blacklist.remove(hashToken(token));
            return false;
        }
        return true;
    }

    private void evictExpired() {
        long now = Instant.now().getEpochSecond();
        blacklist.entrySet().removeIf(e -> now > e.getValue());
    }

    private String hashToken(String token) {
        // Use last 32 chars as key (unique per token, avoids storing full JWT in memory)
        return token.length() > 32 ? token.substring(token.length() - 32) : token;
    }
}
