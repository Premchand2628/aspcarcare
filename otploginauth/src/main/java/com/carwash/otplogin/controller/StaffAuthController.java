package com.carwash.otplogin.controller;

import com.carwash.otplogin.dto.StaffLoginRequest;
import com.carwash.otplogin.dto.StaffRegisterRequest;
import com.carwash.otplogin.service.StaffAuthService;
import com.carwash.otplogin.repository.UserRepository;
import com.carwashcommon.security.JwtTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
@Slf4j
public class StaffAuthController {

    private final StaffAuthService staffAuthService;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;

    @Value("${admin.secret-key:}")
    private String adminSecretKey;

    /**
     * Admin login — validates a pre-shared secret key (set in env.properties)
     * Returns a JWT with ADMIN role for use with protected endpoints.
     *
     * POST /auth/admin/login
     * Body: { "secretKey": "your-admin-secret" }
     */
    @PostMapping("/auth/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> request) {
        String providedKey = request.get("secretKey");

        if (providedKey == null || providedKey.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Secret key is required"));
        }

        if (adminSecretKey == null || adminSecretKey.isBlank()) {
            log.error("Admin login attempted but ADMIN_SECRET_KEY is not configured");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("success", false, "message", "Admin login not configured"));
        }

        if (!java.security.MessageDigest.isEqual(
                adminSecretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                providedKey.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            log.warn("Admin login failed: invalid secret key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Invalid secret key"));
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("ADMIN"));

        String token = jwtTokenService.generateAccessToken("admin", claims);

        log.info("Admin login successful");

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Admin login successful",
                "token", token
        ));
    }

    @PostMapping("/auth/staff/login")
    public ResponseEntity<?> login(@RequestBody StaffLoginRequest request) {
        Map<String, Object> result = staffAuthService.login(request);
        boolean success = (boolean) result.get("success");

        if (!success) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/staff/register")
    public ResponseEntity<?> register(@RequestBody StaffRegisterRequest request) {
        Map<String, Object> result = staffAuthService.register(request);
        boolean success = (boolean) result.get("success");

        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/staff/list")
    public ResponseEntity<?> listStaff() {
        return ResponseEntity.ok(staffAuthService.listAllStaff());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/staff/{staffId}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable String staffId) {
        Map<String, Object> result = staffAuthService.toggleActive(staffId);
        boolean success = (boolean) result.get("success");
        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/stats")
    public ResponseEntity<?> getUserStats() {
        java.time.Instant now = java.time.Instant.now();
        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("last24h", userRepository.countCreatedSince(now.minus(java.time.Duration.ofDays(1))));
        stats.put("last7Days", userRepository.countCreatedSince(now.minus(java.time.Duration.ofDays(7))));
        stats.put("last30Days", userRepository.countCreatedSince(now.minus(java.time.Duration.ofDays(30))));
        return ResponseEntity.ok(stats);
    }
}
