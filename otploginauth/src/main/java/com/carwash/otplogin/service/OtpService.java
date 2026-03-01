package com.carwash.otplogin.service;

import com.carwash.otplogin.model.OtpData;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);
    private static final int MAX_ATTEMPTS = 5;
    private final JavaMailSender mailSender;

    // mobileNumber -> OtpData
    private final ConcurrentMap<String, OtpData> otpStore = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateAndSendOtp(String mobileNumber) {
        String otp = generateOtpValue();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(OTP_EXPIRY);

        OtpData data = new OtpData(otp, expiresAt, now);
        otpStore.put(mobileNumber, data);

        // TODO: Integrate with SMS/Email API
        System.out.println("DEBUG OTP for " + mobileNumber + " = " + otp);

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

        if (data.getOtp().equals(otp)) {
            // Successful verification -> clean up
            otpStore.remove(mobileNumber);
            return VerifyResult.SUCCESS;
        } else {
            return VerifyResult.INVALID;
        }
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

        OtpData data = new OtpData(otp, expiresAt, now);
        otpStore.put(email, data);

        sendEmailOtp(email, otp);
        return otp;
    }

    private void sendEmailOtp(String email, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("ASP Car Care - OTP");
        msg.setText("Your OTP is: " + otp + " (valid 5 minutes)");
        mailSender.send(msg);
    }


}
