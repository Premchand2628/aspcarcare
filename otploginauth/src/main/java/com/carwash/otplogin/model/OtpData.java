package com.carwash.otplogin.model;

import java.time.Instant;

public class OtpData {
    private String otp;
    private Instant expiresAt;
    private int attempts;
    private Instant lastSentAt;

    public OtpData(String otp, Instant expiresAt, Instant lastSentAt) {
        this.otp = otp;
        this.expiresAt = expiresAt;
        this.lastSentAt = lastSentAt;
        this.attempts = 0;
    }

    public String getOtp() {
        return otp;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public Instant getLastSentAt() {
        return lastSentAt;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setLastSentAt(Instant lastSentAt) {
        this.lastSentAt = lastSentAt;
    }
}
