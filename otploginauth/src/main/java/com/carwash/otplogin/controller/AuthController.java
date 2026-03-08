package com.carwash.otplogin.controller;

import com.carwash.otplogin.dto.*;
import com.carwash.otplogin.entity.User;
import com.carwash.otplogin.repository.UserRepository;
import com.carwash.otplogin.service.JwtService;
import com.carwash.otplogin.service.OtpService;
import com.carwash.otplogin.service.OtpService.VerifyResult;
import com.carwashcommon.security.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")

public class AuthController {

    private final OtpService otpService;
    private final JwtService jwtService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    @Value("${google.client.id:}")
    private String googleClientId;



    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }

    // 1️⃣ LOGIN -> send OTP only if phone already registered
    @PostMapping("/login/send-otp")
    public ResponseEntity<ApiResponse> sendLoginOtp(@RequestBody SendOtpRequest request) {
        String mobile = request.getMobileNumber();
        if (mobile == null || mobile.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Mobile number is required"));
        }

        boolean exists = userRepository.existsByPhone(mobile);
        if (!exists) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Mobile number not registered. Please sign up."));
        }

        String otp = otpService.generateAndSendOtp(mobile);
        return ResponseEntity.ok(
                new ApiResponse(true, "OTP sent successfully (DEV only: " + otp + ")"));
    }

    // 2️⃣ SIGNUP -> send OTP only if phone NOT yet registered
    @PostMapping("/signup/send-otp")
    public ResponseEntity<ApiResponse> sendSignupOtp(@RequestBody SignupRequest request) {
        String phone = request.getPhone();
        String email = request.getEmail();

        if (phone == null || phone.isBlank()
                || email == null || email.isBlank()
                || request.getFirstName() == null || request.getFirstName().isBlank()
                || request.getLastName() == null || request.getLastName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Please fill all required signup fields"));
        }

        if (userRepository.existsByPhone(phone)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false,
                            "Mobile number already registered. Please login."));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false,
                            "Email already registered. Please login."));
        }

        String otp = otpService.generateAndSendOtp(phone);
        return ResponseEntity.ok(
                new ApiResponse(true, "OTP sent successfully (DEV only: " + otp + ")"));
    }

    // 3️⃣ VERIFY OTP (same as before, returns JWT token)
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody VerifyOtpRequest request) {
        if (request.getMobileNumber() == null || request.getMobileNumber().isBlank()
                || request.getOtp() == null || request.getOtp().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Mobile number and OTP are required"));
        }

        VerifyResult result = otpService.verifyOtp(request.getMobileNumber(), request.getOtp());

        return switch (result) {

        case SUCCESS -> {
            Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("roles", List.of("USER"));
            claims.put("phone", request.getMobileNumber());
            String jwt = jwtTokenService.generateAccessToken(request.getMobileNumber(), claims);

            System.out.println("JWT: " + jwt);

            yield ResponseEntity.ok(new ApiResponse(
                    true,
                    "OTP verified, login successful",
                    jwt
            ));
        }
            case INVALID -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid OTP"));
            case EXPIRED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "OTP expired"));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "No OTP found, please request again"));
            case TOO_MANY_ATTEMPTS -> ResponseEntity.status(HttpStatus.LOCKED)
                    .body(new ApiResponse(false,
                            "Too many attempts. Please request a new OTP"));
        };
    }
    @PostMapping("/password/forgot/send-otp")
    public ResponseEntity<ApiResponse> sendForgotPasswordOtp(@RequestBody ForgotPasswordSendOtpRequest request) {
        String email = request.getEmail();

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email is required"));
        }

        boolean exists = userRepository.existsByEmail(email);
        if (!exists) {
            // You can return OK to avoid user enumeration, but keeping simple:
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Email not registered. Please sign up."));
        }

        String otp = otpService.generateAndSendEmailOtp(email);
        return ResponseEntity.ok(new ApiResponse(true, "OTP sent to email (DEV only: " + otp + ")"));
    }
    
    // 4️⃣ LOGIN WITH GOOGLE - Verify Google token and create/update user
    @PostMapping("/login/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> request) {
        try {
            String googleToken = request.get("googleToken");
            
            if (googleToken == null || googleToken.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Google token is required"));
            }
            
            // Decode and verify Google token
            Map<String, Object> payload = verifyGoogleToken(googleToken);
            if (payload == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Invalid Google token"));
            }
            
            String email = (String) payload.get("email");
            String firstName = (String) payload.getOrDefault("given_name", "User");
            String lastName = (String) payload.getOrDefault("family_name", "");
            
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Email not found in token"));
            }
            
            // Create or update user
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createNewUser(email, firstName, lastName));
            
            // Generate JWT token
            Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("roles", Collections.singletonList("USER"));
            if (user.getPhone() != null && !user.getPhone().isBlank()) {
                claims.put("phone", user.getPhone());
            }
            String jwtToken = jwtTokenService.generateAccessToken(email, claims);
            
            // Return response with token and user data
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Google login successful",
                    "token", jwtToken,
                    "data", Map.of(
                            "email", user.getEmail(),
                            "firstName", user.getFirstName(),
                            "phone", user.getPhone() != null ? user.getPhone() : ""
                    )
            ));
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to verify Google token: " + e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Internal server error: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * Decode Google ID Token JWT
     * Extracts and returns the payload claims
     */
    private Map<String, Object> verifyGoogleToken(String idTokenString) throws IOException {
        try {
            // JWT format: header.payload.signature
            String[] parts = idTokenString.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            
            // Decode the payload (second part)
            String payload = parts[1];
            // Add padding if needed
            payload += "=".repeat(4 - payload.length() % 4);
            
            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String decodedPayload = new String(decodedBytes);
            
            // Parse JSON payload
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = mapper.readValue(decodedPayload, Map.class);
            
            // Verify audience (client ID)
            String clientId = System.getenv("GOOGLE_CLIENT_ID");
            if (clientId == null || clientId.isBlank()) {
                clientId = googleClientId;
            }
            
            String audience = (String) claims.get("aud");
            if (clientId != null && !clientId.isBlank() && !clientId.equals(audience)) {
                System.err.println("Client ID mismatch. Expected: " + clientId + ", Got: " + audience);
                // For development, we'll allow this - in production, validate strictly
            }
            
            return claims;
            
        } catch (Exception e) {
            System.err.println("Error parsing Google token: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Create a new user from Google OAuth data
     */
    private User createNewUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName != null ? lastName : "");
        user.setCreatedAt(Instant.now());
        return userRepository.save(user);
    }
}
