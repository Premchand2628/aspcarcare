package com.carwash.otplogin.service;

import com.carwash.otplogin.model.OtpData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(5);
    private static final Duration PASSWORD_RESET_VERIFIED_EXPIRY = Duration.ofMinutes(5);
    private static final Duration EMAIL_UPDATE_VERIFIED_EXPIRY = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);
    private static final int MAX_ATTEMPTS = 5;
    private final JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.from}")
    private String mailFrom;

    // Server-side secret used to HMAC-hash OTPs before they touch memory.
    // Re-uses jwt.secret so no new config / no new secret rotation burden.
    @org.springframework.beans.factory.annotation.Value("${jwt.secret}")
    private String otpHashSecret;

    // mobileNumber -> OtpData   (stored value is HMAC-SHA256 hex, never plaintext)
    private final ConcurrentMap<String, OtpData> otpStore = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Instant> passwordResetVerifiedStore = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Instant> emailUpdateVerifiedStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String generateAndSendOtp(String mobileNumber) {
        String otp = generateOtpValue();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(OTP_EXPIRY);

        OtpData data = new OtpData(hashOtp(otp), expiresAt, now);
        otpStore.put(mobileNumber, data);

        // TODO: Integrate with SMS API. OTP value is never logged.
        log.debug("OTP generated for subject hash={}", Integer.toHexString(mobileNumber.hashCode()));

        return otp;
    }

    public boolean canResend(String mobileNumber) {
        OtpData data = otpStore.get(mobileNumber);
        if (data == null) return true;
        Instant now = Instant.now();
        return now.isAfter(data.getLastSentAt().plus(RESEND_COOLDOWN));
    }

    public String resendOtp(String mobileNumber) {
        if (!canResend(mobileNumber)) {
            return null;
        }
        return generateAndSendOtp(mobileNumber);
    }

    public VerifyResult verifyOtp(String mobileNumber, String otp) {
        OtpData data = otpStore.get(mobileNumber);
        if (data == null) {
            return VerifyResult.NOT_FOUND;
        }

        // Expired?
        if (Instant.now().isAfter(data.getExpiresAt())) {
            otpStore.remove(mobileNumber);
            return VerifyResult.EXPIRED;
        }

        // Too many attempts
        if (data.getAttempts() >= MAX_ATTEMPTS) {
            otpStore.remove(mobileNumber);
            return VerifyResult.TOO_MANY_ATTEMPTS;
        }

        data.incrementAttempts();

        if (constantTimeEquals(data.getOtp(), hashOtp(otp))) {
            // Successful verification -> clean up
            otpStore.remove(mobileNumber);
            return VerifyResult.SUCCESS;
        } else {
            return VerifyResult.INVALID;
        }
    }

    private String hashOtp(String otp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    otpHashSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(otp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("OTP hashing failed", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] ba = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        if (ba.length != bb.length) return false;
        int r = 0;
        for (int i = 0; i < ba.length; i++) r |= ba[i] ^ bb[i];
        return r == 0;
    }

    private String generateOtpValue() {
        int min = (int) Math.pow(10, OTP_LENGTH - 1);
        int max = (int) Math.pow(10, OTP_LENGTH) - 1;
        int value = random.nextInt(max - min + 1) + min;
        return String.valueOf(value);
    }

    public enum VerifyResult {
        SUCCESS,
        INVALID,
        EXPIRED,
        NOT_FOUND,
        TOO_MANY_ATTEMPTS
    }

    

    public String generateAndSendEmailOtp(String email) {
        String otp = generateOtpValue();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(OTP_EXPIRY);

        OtpData data = new OtpData(hashOtp(otp), expiresAt, now);
        otpStore.put(email, data);

        sendEmailOtp(email, otp);
        return otp;
    }

    public void markPasswordResetOtpVerified(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        passwordResetVerifiedStore.put(email.trim().toLowerCase(), Instant.now().plus(PASSWORD_RESET_VERIFIED_EXPIRY));
    }

    public boolean consumePasswordResetOtpVerified(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String key = email.trim().toLowerCase();
        Instant verifiedUntil = passwordResetVerifiedStore.remove(key);
        if (verifiedUntil == null) {
            return false;
        }

        return Instant.now().isBefore(verifiedUntil);
    }

    public void markEmailUpdateOtpVerified(String phone, String email) {
        String key = buildEmailUpdateKey(phone, email);
        if (key == null) {
            return;
        }
        emailUpdateVerifiedStore.put(key, Instant.now().plus(EMAIL_UPDATE_VERIFIED_EXPIRY));
    }

    public boolean consumeEmailUpdateOtpVerified(String phone, String email) {
        String key = buildEmailUpdateKey(phone, email);
        if (key == null) {
            return false;
        }

        Instant verifiedUntil = emailUpdateVerifiedStore.remove(key);
        if (verifiedUntil == null) {
            return false;
        }

        return Instant.now().isBefore(verifiedUntil);
    }

    private String buildEmailUpdateKey(String phone, String email) {
        if (phone == null || phone.isBlank() || email == null || email.isBlank()) {
            return null;
        }
        return phone.trim() + "|" + email.trim().toLowerCase();
    }

    private void sendEmailOtp(String email, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(mailFrom);
        msg.setTo(email);
        msg.setSubject("ASP Car Care - OTP");
        msg.setText("Your OTP is: " + otp + " (valid 5 minutes)");
        mailSender.send(msg);
    }


}
