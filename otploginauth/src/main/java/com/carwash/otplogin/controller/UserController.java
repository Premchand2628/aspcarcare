package com.carwash.otplogin.controller;

import com.carwash.otplogin.dto.ApiResponse;
import com.carwash.otplogin.dto.SignupRequest;
import com.carwash.otplogin.dto.UpdateProfileRequest;
import com.carwash.otplogin.dto.UserProfileResponse;
import com.carwash.otplogin.entity.User;
import com.carwash.otplogin.repository.UserRepository;
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
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    // ==========================================================
    // ✅ Helper: extract phone from JWT (sub)
    // ==========================================================
    private String getPhoneFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal p) {
            return p.getPhone();
        }

        // fallback - sometimes principal can be String
        if (principal instanceof String s && !s.isBlank()) {
            return s;
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
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
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
        user.setAge(req.getAge());
        user.setPhone(req.getPhone().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

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
    public ResponseEntity<ApiResponse> updatePhoneByEmail(@RequestBody Map<String, String> request) {
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
        if (existingPhone.isPresent() && !existingPhone.get().getEmail().equals(email.trim())) {
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

        return ResponseEntity.ok(new ApiResponse(true, "Phone number updated successfully", jwtToken));
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
