package com.carwashcommon.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenMinutes = 20;
    private long refreshTokenDays = 7;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getAccessTokenMinutes() { return accessTokenMinutes; }
    public void setAccessTokenMinutes(long accessTokenMinutes) { this.accessTokenMinutes = accessTokenMinutes; }

    public long getRefreshTokenDays() { return refreshTokenDays; }
    public void setRefreshTokenDays(long refreshTokenDays) { this.refreshTokenDays = refreshTokenDays; }
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
