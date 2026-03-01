package com.carwashcommon.security;

import io.jsonwebtoken.Claims;

public class JwtUserPrincipal {
    private final String phone;
    private final Claims claims;

    public JwtUserPrincipal(String phone, Claims claims) {
        this.phone = phone;
        this.claims = claims;
    }

    public String getPhone() { return phone; }
    public Claims getClaims() { return claims; }
}
