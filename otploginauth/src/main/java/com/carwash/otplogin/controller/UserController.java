package com.carwash.otplogin.controller;

import com.carwash.otplogin.dto.ApiResponse;
import com.carwash.otplogin.dto.ProfileEmailSendOtpRequest;
import com.carwash.otplogin.dto.ProfileEmailVerifyOtpRequest;
import com.carwash.otplogin.dto.SaveDefaultBookingRequest;
import com.carwash.otplogin.dto.SignupRequest;
import com.carwash.otplogin.dto.UpdateProfileRequest;
import com.carwash.otplogin.dto.UserProfileResponse;
import com.carwash.otplogin.entity.User;
import com.carwash.otplogin.repository.UserRepository;
import com.carwash.otplogin.service.OtpService;
import com.carwash.otplogin.service.OtpService.VerifyResult;
import com.carwashcommon.security.JwtUserPrincipal;
import com.carwashcommon.security.JwtTokenService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final OtpService otpService;

    public UserController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenService jwtTokenService,
                          OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.otpService = otpService;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    // ==========================================================
    // ✅ Helper: extract phone from JWT (sub)
    // ==========================================================
    private String getPhoneFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal p) {
            String candidate = p.getPhone();
            if (candidate == null || candidate.isBlank()) {
                return null;
            }
            candidate = candidate.trim();
            if (candidate.contains("@")) {
                Optional<User> byEmail = userRepository.findByEmail(candidate);
                if (byEmail.isPresent() && byEmail.get().getPhone() != null && !byEmail.get().getPhone().isBlank()) {
                    return byEmail.get().getPhone();
                }
                if (p.getClaims() != null) {
                    Object phoneFromClaim = p.getClaims().get("phone");
                    if (phoneFromClaim != null) {
                        String claimPhone = String.valueOf(phoneFromClaim).trim();
                        if (!claimPhone.isBlank()) {
                            return claimPhone;
                        }
                    }
                }
            }
            return candidate;
        }

        // fallback - sometimes principal can be String
        if (principal instanceof String s && !s.isBlank()) {
            String candidate = s.trim();
            if (candidate.contains("@")) {
                Optional<User> byEmail = userRepository.findByEmail(candidate);
                if (byEmail.isPresent() && byEmail.get().getPhone() != null && !byEmail.get().getPhone().isBlank()) {
                    return byEmail.get().getPhone();
                }
            }
            return candidate;
        }

        return null;
    }

    // ==========================================================
    // 🔹 1) GET PROFILE (JWT-secured)
    // URL: GET /users/profile
    // Header: Authorization: Bearer <token>
    // ==========================================================
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        String phone = getPhoneFromAuth(authentication);
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Unauthorized: token missing or invalid"));
        }

        Optional<User> optUser = userRepository.findByPhone(phone);
        if (optUser.isEmpty() && phone.contains("@")) {
            optUser = userRepository.findByEmail(phone);
        }
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found for phone: " + phone));
        }

        User user = optUser.get();

        UserProfileResponse resp = new UserProfileResponse();
        resp.setFirstName(user.getFirstName());
        resp.setLastName(user.getLastName());
        resp.setEmail(user.getEmail());
        resp.setAge(user.getAge());
        resp.setPhone(user.getPhone());
        resp.setAddress(user.getAddress());
        resp.setCarNumber(user.getCarNumber());
        resp.setCarAddressDefaultFlag(user.getCarAddressDefaultFlag());

        return ResponseEntity.ok(resp);
    }

    // ==========================================================
    // 🔹 2) UPDATE PROFILE (JWT-secured)
    // URL: PUT /users/profile
    // Header: Authorization: Bearer <token>
    // Body: { firstName?, lastName?, password? }
    // NOTE: phone is derived from JWT (not request body)
    // ==========================================================
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(@RequestBody UpdateProfileRequest req,
                                                     Authentication authentication) {

        String phone = getPhoneFromAuth(authentication);
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Unauthorized: token missing or invalid"));
        }

        Optional<User> optUser = userRepository.findByPhone(phone);
        if (optUser.isEmpty() && phone.contains("@")) {
            optUser = userRepository.findByEmail(phone);
        }
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found for phone: " + phone));
        }

        User user = optUser.get();

        if (req.getFirstName() != null && !req.getFirstName().isBlank()) {
            user.setFirstName(req.getFirstName().trim());
        }
        if (req.getLastName() != null && !req.getLastName().isBlank()) {
            user.setLastName(req.getLastName().trim());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            String nextEmail = normalizeEmail(req.getEmail());
            String currentEmail = normalizeEmail(user.getEmail());
            Optional<User> existingEmailUser = userRepository.findByEmail(nextEmail);
            if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse(false, "Email already registered to another user"));
            }

            boolean emailChanged = currentEmail == null || !currentEmail.equals(nextEmail);
            if (emailChanged && !otpService.consumeEmailUpdateOtpVerified(user.getPhone(), nextEmail)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Please verify OTP sent to updated email before saving"));
            }

            user.setEmail(nextEmail);
        }
        if (req.getAddress() != null) {
            user.setAddress(req.getAddress().trim());
        }
        if (req.getCarNumber() != null) {
            user.setCarNumber(req.getCarNumber().trim());
        }
        if (req.getCarAddressDefaultFlag() != null && !req.getCarAddressDefaultFlag().isBlank()) {
            String normalizedFlag = req.getCarAddressDefaultFlag().trim().toUpperCase();
            user.setCarAddressDefaultFlag("Y".equals(normalizedFlag) ? "Y" : "N");
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
    }

    @PostMapping("/profile/default-booking")
    public ResponseEntity<ApiResponse> saveDefaultBookingDetails(@RequestBody SaveDefaultBookingRequest req,
                                                                 Authentication authentication) {
        String phone = getPhoneFromAuth(authentication);
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Unauthorized: token missing or invalid"));
        }

        Optional<User> optUser = userRepository.findByPhone(phone);
        if (optUser.isEmpty() && phone.contains("@")) {
            optUser = userRepository.findByEmail(phone);
        }
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found for phone: " + phone));
        }

        if (req == null || !Boolean.TRUE.equals(req.getSaveAsDefault())) {
            return ResponseEntity.ok(new ApiResponse(true, "No default profile changes requested"));
        }

        String address = req.getAddress() == null ? "" : req.getAddress().trim();
        String carNumber = req.getCarNumber() == null ? "" : req.getCarNumber().trim();

        if (address.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Address is required to save defaults"));
        }

        if (carNumber.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Car number is required to save defaults"));
        }

        User user = optUser.get();
        user.setAddress(address);
        user.setCarNumber(carNumber);
        user.setCarAddressDefaultFlag("Y");
        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "Default booking details saved successfully"));
    }

    @PostMapping("/profile/email/send-otp")
    public ResponseEntity<ApiResponse> sendProfileEmailOtp(@RequestBody ProfileEmailSendOtpRequest request,
                                                           Authentication authentication) {
        String phone = getPhoneFromAuth(authentication);
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Unauthorized: token missing or invalid"));
        }

        if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Updated email is required"));
        }

        Optional<User> optUser = userRepository.findByPhone(phone);
        if (optUser.isEmpty() && phone.contains("@")) {
            optUser = userRepository.findByEmail(phone);
        }
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found for phone: " + phone));
        }

        User user = optUser.get();
        String nextEmail = normalizeEmail(request.getEmail());
        String currentEmail = normalizeEmail(user.getEmail());
        if (nextEmail == null || !nextEmail.contains("@")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Please enter a valid email"));
        }
        if (currentEmail != null && currentEmail.equals(nextEmail)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Updated email must be different from current email"));
        }

        Optional<User> existingEmailUser = userRepository.findByEmail(nextEmail);
        if (existingEmailUser.isPresent() && !existingEmailUser.get().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Email already registered to another user"));
        }

        String otp = otpService.generateAndSendEmailOtp(nextEmail);
        return ResponseEntity.ok(new ApiResponse(true, "OTP sent to updated email (DEV only: " + otp + ")"));
    }

    @PostMapping("/profile/email/verify-otp")
    public ResponseEntity<ApiResponse> verifyProfileEmailOtp(@RequestBody ProfileEmailVerifyOtpRequest request,
                                                             Authentication authentication) {
        String phone = getPhoneFromAuth(authentication);
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Unauthorized: token missing or invalid"));
        }

        if (request == null || request.getEmail() == null || request.getEmail().isBlank()
                || request.getOtp() == null || request.getOtp().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email and OTP are required"));
        }

        Optional<User> optUser = userRepository.findByPhone(phone);
        if (optUser.isEmpty() && phone.contains("@")) {
            optUser = userRepository.findByEmail(phone);
        }
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found for phone: " + phone));
        }

        String nextEmail = normalizeEmail(request.getEmail());
        VerifyResult result = otpService.verifyOtp(nextEmail, request.getOtp().trim());
        return switch (result) {
            case SUCCESS -> {
                otpService.markEmailUpdateOtpVerified(phone, nextEmail);
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

    // ==========================================================
    // 🔹 3) COMPLETE SIGNUP (public)
    // URL: POST /users/signup
    // ==========================================================
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> completeSignup(@RequestBody SignupRequest req) {

        if (req.getPhone() == null || req.getPhone().isBlank()
                || req.getEmail() == null || req.getEmail().isBlank()
                || req.getFirstName() == null || req.getFirstName().isBlank()
                || req.getLastName() == null || req.getLastName().isBlank()
        ) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Please fill all required fields"));
        }

        // duplicates
        if (userRepository.existsByPhone(req.getPhone())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Mobile number already registered."));
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Email already registered."));
        }

        if (req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Password is required"));
        }

        User user = new User();
        user.setFirstName(req.getFirstName().trim());
        user.setLastName(req.getLastName().trim());
        user.setEmail(req.getEmail().trim());
        user.setPhone(req.getPhone().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setAddress(req.getAddress() == null ? null : req.getAddress().trim());
        user.setCarNumber(req.getCarNumber() == null ? null : req.getCarNumber().trim());
        String signupDefaultFlag = req.getCarAddressDefaultFlag();
        if (signupDefaultFlag != null && !signupDefaultFlag.isBlank()) {
            signupDefaultFlag = signupDefaultFlag.trim().toUpperCase();
            user.setCarAddressDefaultFlag("Y".equals(signupDefaultFlag) ? "Y" : "N");
        } else {
            user.setCarAddressDefaultFlag("N");
        }

        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "Signup completed & profile saved successfully."));
    }

    // ==========================================================
    // ✅ Optional: Simple auth check endpoint
    // URL: GET /users/me
    // ==========================================================
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        String phone = getPhoneFromAuth(authentication);
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Unauthorized"));
        }
        return ResponseEntity.ok(new ApiResponse(true, "Authenticated", phone));
    }

    // ==========================================================
    // 🔹 UPDATE PHONE NUMBER BY EMAIL (for Google OAuth users)
    // URL: POST /users/update-phone
    // Body: { email, phone }
    // Purpose: Google OAuth users add phone number on Booking Review page
    // ==========================================================
    @PostMapping("/update-phone")
    public ResponseEntity<?> updatePhoneByEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String phone = request.get("phone");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Email is required"));
        }
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Phone number is required"));
        }

        // Validate phone format (10 digits)
        if (!phone.matches("^\\d{10}$")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Phone number must be 10 digits"));
        }

        // Check if phone is already registered to another user
        Optional<User> existingPhone = userRepository.findByPhone(phone);
        boolean isNewPhone = existingPhone.isEmpty();
        if (!isNewPhone && !existingPhone.get().getEmail().equals(email.trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, "Phone number already registered to another user"));
        }

        // Find user by email and update phone
        Optional<User> optUser = userRepository.findByEmail(email.trim());
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found with email: " + email));
        }

        User user = optUser.get();
        user.setPhone(phone.trim());
        userRepository.save(user);

        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("roles", java.util.List.of("USER"));
        claims.put("phone", user.getPhone());
        String jwtToken = jwtTokenService.generateAccessToken(user.getEmail(), claims);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Phone number updated successfully",
                "token", jwtToken,
                "isNewPhone", isNewPhone
        ));
    }
}
//package com.carwash.otplogin.controller;
//
//import com.carwash.otplogin.*;
//import com.carwash.otplogin.dto.ApiResponse;
//import com.carwash.otplogin.dto.SignupRequest;
//import com.carwash.otplogin.dto.UpdateProfileRequest;
//import com.carwash.otplogin.dto.UserProfileResponse;
//import com.carwash.otplogin.entity.User;
//import com.carwash.otplogin.repository.UserRepository;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/users")
//@CrossOrigin(origins = "*")
//public class UserController {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    // 🔹 1) GET PROFILE - used by dashboard & edit-profile page
//    // URL: GET /users/profile?phone=1234567890
//    @GetMapping("/profile")
//    public ResponseEntity<?> getProfile(@RequestParam("phone") String phone) {
//        if (phone == null || phone.isBlank()) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, "Phone number is required"));
//        }
//
//        Optional<User> optUser = userRepository.findByPhone(phone);
//        if (optUser.isEmpty()) {
//            return ResponseEntity.status(404)
//                    .body(new ApiResponse(false, "User not found for phone: " + phone));
//        }
//
//        User user = optUser.get();
//
//        UserProfileResponse resp = new UserProfileResponse();
//        resp.setFirstName(user.getFirstName());
//        resp.setLastName(user.getLastName());
//        resp.setEmail(user.getEmail());
//        resp.setAge(user.getAge());
//        resp.setPhone(user.getPhone());
//        //resp.setPassword(user.getPassword()); // ⚠️ only because you want to edit it
//
//        return ResponseEntity.ok(resp);
//    }
//
//    // 🔹 2) UPDATE PROFILE - only firstName, lastName, password editable
//    // URL: PUT /users/profile
//    @PutMapping("/profile")
//    public ResponseEntity<ApiResponse> updateProfile(@RequestBody UpdateProfileRequest req) {
//
//        if (req.getPhone() == null || req.getPhone().isBlank()) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, "Phone number is required to update profile"));
//        }
//
//        Optional<User> optUser = userRepository.findByPhone(req.getPhone());
//        if (optUser.isEmpty()) {
//            return ResponseEntity.status(404)
//                    .body(new ApiResponse(false, "User not found for phone: " + req.getPhone()));
//        }
//
//        User user = optUser.get();
//
//        if (req.getFirstName() != null && !req.getFirstName().isBlank()) {
//            user.setFirstName(req.getFirstName());
//        }
//        if (req.getLastName() != null && !req.getLastName().isBlank()) {
//            user.setLastName(req.getLastName());
//        }
//        if (req.getPassword() != null && !req.getPassword().isBlank()) {
//            // 🔒 Hash the new password instead of storing plain text
//            user.setPassword(passwordEncoder.encode(req.getPassword()));
//        }
//
//        userRepository.save(user);
//
//        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
//    }
//    @PostMapping("/signup")
//    public ResponseEntity<ApiResponse> completeSignup(@RequestBody SignupRequest req) {
//
//        // Basic validation
//        if (req.getPhone() == null || req.getPhone().isBlank()
//            || req.getEmail() == null || req.getEmail().isBlank()
//            || req.getFirstName() == null || req.getFirstName().isBlank()
//            || req.getLastName() == null || req.getLastName().isBlank()
//        ) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, "Please fill all required fields"));
//        }
//
//        // If already exists, do NOT create again
//        if (userRepository.existsByPhone(req.getPhone())) {
//            return ResponseEntity.status(409)
//                    .body(new ApiResponse(false, "Mobile number already registered."));
//        }
//        if (userRepository.existsByEmail(req.getEmail())) {
//            return ResponseEntity.status(409)
//                    .body(new ApiResponse(false, "Email already registered."));
//        }
//
//        User user = new User();
//        user.setFirstName(req.getFirstName());
//        user.setLastName(req.getLastName());
//        user.setEmail(req.getEmail());
//        user.setAge(req.getAge());
//        user.setPhone(req.getPhone());
//
//        // 🔒 This is the important line:
//        user.setPassword(passwordEncoder.encode(req.getPassword()));
//
//        userRepository.save(user);
//
//        return ResponseEntity.ok(
//            new ApiResponse(true, "Signup completed & profile saved successfully.")
//        );
//    }
//
//
//}
