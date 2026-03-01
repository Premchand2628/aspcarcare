package com.carwashcommon.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenMinutes = 60;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getAccessTokenMinutes() { return accessTokenMinutes; }
    public void setAccessTokenMinutes(long accessTokenMinutes) { this.accessTokenMinutes = accessTokenMinutes; }
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
