package com.carwash.rates.common;

public enum WashLevel {
    BASIC, FOAM, PREMIUM;

    public static WashLevel from(String v) {
        return WashLevel.valueOf(v.trim().toUpperCase());
    }
}
