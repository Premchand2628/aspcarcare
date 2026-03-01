package com.carwash.rates.common;

public enum VehicleType {
    HATCHBACK, SEDAN, SUV, MPV, PICKUP, BIKE;

    public static VehicleType from(String v) {
        return VehicleType.valueOf(v.trim().toUpperCase());
    }
}
