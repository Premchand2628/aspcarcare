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
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final OtpService otpService;
    private final JwtService jwtService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.client.id:}")
    private String googleClientId;

    @Value("${facebook.app.id:}")
    private String facebookAppId;

    @Value("${facebook.app.secret:}")
    private String facebookAppSecret;



    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) return local.charAt(0) + "*" + domain;
        return local.charAt(0) + "*".repeat(local.length() - 2) + local.charAt(local.length() - 1) + domain;
    }

    // 🔹 CHECK PHONE (public - for OAuth phone linking flow)
    // URL: POST /auth/check-phone
    // Body: { "phone": "1234567890" }
    @PostMapping("/check-phone")
    public ResponseEntity<?> checkPhone(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        if (phone == null || phone.isBlank() || !phone.matches("^\\d{10}$")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Valid 10-digit phone number is required"));
        }
        Optional<User> optUser = userRepository.findByPhone(phone.trim());
        if (optUser.isPresent()) {
            String email = optUser.get().getEmail();
            String masked = email != null ? maskEmail(email) : "";
            return ResponseEntity.ok(Map.of("exists", true, "email", masked));
        }
        return ResponseEntity.ok(Map.of("exists", false));
    }

    // 🔹 SEND OTP (generic - for OAuth phone linking flow)
    // URL: POST /auth/send-otp-generic
    // Body: { "mobileNumber": "1234567890" }
    @PostMapping("/send-otp-generic")
    public ResponseEntity<ApiResponse> sendGenericOtp(@RequestBody SendOtpRequest request) {
        String mobile = request.getMobileNumber();
        if (mobile == null || mobile.isBlank() || !mobile.matches("^\\d{10}$")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Valid 10-digit mobile number is required"));
        }
        String otp = otpService.generateAndSendOtp(mobile);
        return ResponseEntity.ok(
                new ApiResponse(true, "OTP sent successfully (DEV only: " + otp + ")"));
    }

    // 1️⃣ LOGIN -> send OTP only if phone already registered
    @PostMapping("/login/send-otp")
    public ResponseEntity<ApiResponse> sendLoginOtp(@RequestBody SendOtpRequest request, HttpServletRequest httpRequest) {
        long startMs = System.currentTimeMillis();
        String txId   = httpRequest.getHeader("x-transaction-id");
        String url    = httpRequest.getRequestURI();
        String mobile = request.getMobileNumber();

        log.info("REQUEST   txId={}  mobile={}  url={}", txId, mobile, url);

        if (mobile == null || mobile.isBlank()) {
            log.info("RESPONSE  txId={}  status=400  durationMs={}  otp=-", txId, System.currentTimeMillis() - startMs);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Mobile number is required"));
        }

        boolean exists = userRepository.existsByPhone(mobile);
        if (!exists) {
            log.info("RESPONSE  txId={}  status=404  durationMs={}  otp=-", txId, System.currentTimeMillis() - startMs);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Mobile number not registered. Please sign up."));
        }

        String otp = otpService.generateAndSendOtp(mobile);
        log.info("RESPONSE  txId={}  status=200  durationMs={}  otp={}", txId, System.currentTimeMillis() - startMs, otp);
        return ResponseEntity.ok(
                new ApiResponse(true, "OTP sent successfully (DEV only: " + otp + ")"));
    }

        @PostMapping("/login/email")
        public ResponseEntity<?> loginWithEmail(@RequestBody EmailLoginRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()
            || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Email and password are required"));
        }

        String email = request.getEmail().trim().toLowerCase();
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "Invalid email or password"));
        }

        User user = optionalUser.get();
        String encodedPassword = user.getPassword();
        if (encodedPassword == null || encodedPassword.isBlank()
            || !passwordEncoder.matches(request.getPassword(), encodedPassword)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "Invalid email or password"));
        }

        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("roles", Collections.singletonList("USER"));
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            claims.put("phone", user.getPhone());
        }

        String jwtToken = jwtTokenService.generateAccessToken(email, claims);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Email login successful",
            "token", jwtToken,
            "data", Map.of(
                "email", user.getEmail() == null ? "" : user.getEmail(),
                "firstName", user.getFirstName() == null ? "" : user.getFirstName(),
                "phone", user.getPhone() == null ? "" : user.getPhone()
            )
        ));
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

        email = email.trim().toLowerCase();

        boolean exists = userRepository.existsByEmail(email);
        if (!exists) {
            // You can return OK to avoid user enumeration, but keeping simple:
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Email not registered. Please sign up."));
        }

        String otp = otpService.generateAndSendEmailOtp(email);
        return ResponseEntity.ok(new ApiResponse(true, "OTP sent to email (DEV only: " + otp + ")"));
    }

    @PostMapping("/password/forgot/verify-otp")
    public ResponseEntity<ApiResponse> verifyForgotPasswordOtp(@RequestBody ForgotPasswordVerifyOtpRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()
                || request.getOtp() == null || request.getOtp().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email and OTP are required"));
        }

        String email = request.getEmail().trim().toLowerCase();
        VerifyResult result = otpService.verifyOtp(email, request.getOtp().trim());

        return switch (result) {
            case SUCCESS -> {
                otpService.markPasswordResetOtpVerified(email);
                yield ResponseEntity.ok(new ApiResponse(true, "OTP verified successfully"));
            }
            case INVALID -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid OTP"));
            case EXPIRED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "OTP expired. Please request a new OTP"));
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "No OTP found. Please request again"));
            case TOO_MANY_ATTEMPTS -> ResponseEntity.status(HttpStatus.LOCKED)
                    .body(new ApiResponse(false, "Too many attempts. Please request a new OTP"));
        };
    }

    @PostMapping("/password/forgot/reset")
    public ResponseEntity<ApiResponse> resetForgotPassword(@RequestBody ForgotPasswordResetRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()
                || request.getNewPassword() == null || request.getNewPassword().isBlank()
                || request.getConfirmPassword() == null || request.getConfirmPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email, new password and confirm password are required"));
        }

        String email = request.getEmail().trim().toLowerCase();
        String newPassword = request.getNewPassword().trim();
        String confirmPassword = request.getConfirmPassword().trim();

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "New password and confirm password must match"));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Password must be at least 6 characters"));
        }

        if (!otpService.consumePasswordResetOtpVerified(email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "OTP verification required or session expired"));
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Email not registered. Please sign up."));
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "Password updated successfully"));
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
     * Verify Google ID Token using Google's public keys
     */
    private Map<String, Object> verifyGoogleToken(String idTokenString) throws IOException {
        try {
            String clientId = System.getenv("GOOGLE_CLIENT_ID");
            if (clientId == null || clientId.isBlank()) {
                clientId = googleClientId;
            }

            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier =
                    new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(
                            new com.google.api.client.http.javanet.NetHttpTransport(),
                            com.google.api.client.json.gson.GsonFactory.getDefaultInstance())
                            .setAudience(clientId != null && !clientId.isBlank()
                                    ? Collections.singletonList(clientId)
                                    : Collections.emptyList())
                            .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken =
                    verifier.verify(idTokenString);

            if (idToken == null) {
                log.warn("Google token verification failed: invalid signature or expired");
                return null;
            }

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload =
                    idToken.getPayload();

            Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("email", payload.getEmail());
            claims.put("given_name", payload.get("given_name"));
            claims.put("family_name", payload.get("family_name"));
            claims.put("sub", payload.getSubject());
            claims.put("aud", payload.getAudience());

            return claims;

        } catch (java.io.IOException | java.security.GeneralSecurityException e) {
            log.error("Error verifying Google token", e);
            return null;
        } catch (Exception e) {
            log.error("Error verifying Google token: {}", e.getMessage());
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
        user.setCarAddressDefaultFlag("N");
        user.setCreatedAt(Instant.now());
        return userRepository.save(user);
    }

    // 5\u20e3\ufe0f LOGIN WITH FACEBOOK - Verify Facebook token and create/update user
    @PostMapping("/login/facebook")
    public ResponseEntity<?> loginWithFacebook(@RequestBody Map<String, String> request) {
        try {
            String facebookToken = request.get("facebookToken");

            if (facebookToken == null || facebookToken.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Facebook token is required"));
            }

            Map<String, Object> fbProfile = verifyFacebookToken(facebookToken);
            if (fbProfile == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Invalid Facebook token"));
            }

            String email = (String) fbProfile.get("email");
            String firstName = (String) fbProfile.getOrDefault("first_name", "User");
            String lastName = (String) fbProfile.getOrDefault("last_name", "");

            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Email not available from Facebook. Please ensure email permission is granted."));
            }

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createNewUser(email, firstName, lastName));

            Map<String, Object> claims = new java.util.HashMap<>();
            claims.put("roles", Collections.singletonList("USER"));
            if (user.getPhone() != null && !user.getPhone().isBlank()) {
                claims.put("phone", user.getPhone());
            }
            String jwtToken = jwtTokenService.generateAccessToken(email, claims);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Facebook login successful",
                    "token", jwtToken,
                    "data", Map.of(
                            "email", user.getEmail(),
                            "firstName", user.getFirstName(),
                            "phone", user.getPhone() != null ? user.getPhone() : ""
                    )
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Failed to verify Facebook token: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Internal server error: " + e.getMessage()));
        }
    }

    private Map<String, Object> verifyFacebookToken(String accessToken) throws IOException {
        try {
            String appId = System.getenv("FACEBOOK_APP_ID");
            String appSecret = System.getenv("FACEBOOK_APP_SECRET");
            if (appId == null || appId.isBlank()) appId = facebookAppId;
            if (appSecret == null || appSecret.isBlank()) appSecret = facebookAppSecret;

            if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()) return null;

            HttpClient client = HttpClient.newHttpClient();

            String debugUrl = "https://graph.facebook.com/debug_token?input_token=" + accessToken + "&access_token=" + appId + "|" + appSecret;
            HttpRequest debugReq = HttpRequest.newBuilder().uri(URI.create(debugUrl)).header("Accept", "application/json").GET().build();
            HttpResponse<String> debugResp = client.send(debugReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (debugResp.statusCode() != 200) return null;

            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> debugBody = mapper.readValue(debugResp.body(), Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> debugData = (Map<String, Object>) debugBody.get("data");
            if (debugData == null) return null;

            Boolean isValid = (Boolean) debugData.get("is_valid");
            if (isValid == null || !isValid) return null;

            String tokenAppId = String.valueOf(debugData.get("app_id"));
            if (!appId.equals(tokenAppId)) return null;

            String profileUrl = "https://graph.facebook.com/me?fields=id,email,first_name,last_name&access_token=" + accessToken;
            HttpRequest profileReq = HttpRequest.newBuilder().uri(URI.create(profileUrl)).header("Accept", "application/json").GET().build();
            HttpResponse<String> profileResp = client.send(profileReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (profileResp.statusCode() != 200) return null;

            @SuppressWarnings("unchecked")
            Map<String, Object> profile = mapper.readValue(profileResp.body(), Map.class);
            return profile;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
