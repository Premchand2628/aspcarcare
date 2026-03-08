package com.carwash.invitation.controller;

import com.carwash.invitation.dto.*;
import com.carwash.invitation.service.ReferralCouponService;
import com.carwashcommon.security.JwtUserPrincipal;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/coupons")
@CrossOrigin(origins = "*")
public class ReferralCouponController {

    private final ReferralCouponService service;

    public ReferralCouponController(ReferralCouponService service) {
        this.service = service;
    }

    // Generate coupon for inviter
    @PostMapping("/generate")
    public ResponseEntity<GenerateCouponResponse> generate(@RequestBody GenerateCouponRequest req) {
        return ResponseEntity.ok(service.generate(req));
    }

    // Validate coupon at checkout (before payment)
    @PostMapping("/validate")
    public ResponseEntity<ValidateCouponResponse> validate(@RequestBody ValidateCouponRequest req) {
        return ResponseEntity.ok(service.validate(req));
    }

    // Redeem coupon after payment success / booking created
    @PostMapping("/redeem")
    public ResponseEntity<?> redeem(@RequestBody RedeemCouponRequest req) {
        service.redeem(req);
        return ResponseEntity.ok().body("Coupon redeemed successfully");
    }

    @GetMapping("/referral-details")
    public ResponseEntity<?> referralDetails(@RequestParam String userPhone,
                                             Authentication authentication) {
        String resolvedPhone = resolvePhone(authentication);
        if (resolvedPhone == null || resolvedPhone.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        if (userPhone == null || userPhone.isBlank()) {
            return ResponseEntity.badRequest().body("userPhone is required");
        }

        if (!normalizePhone(resolvedPhone).equals(normalizePhone(userPhone))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
        }

        Map<String, Object> details = service.referralDetails(resolvedPhone);
        return ResponseEntity.ok(details);
    }

    private String resolvePhone(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            Claims claims = jwtUserPrincipal.getClaims();
            Object phoneClaim = claims != null ? claims.get("phone") : null;

            if (phoneClaim != null && !phoneClaim.toString().isBlank()) {
                return phoneClaim.toString();
            }

            String subject = jwtUserPrincipal.getPhone();
            if (subject != null && !subject.contains("@")) {
                return subject;
            }
        }

        if (principal instanceof String s && !s.contains("@")) {
            return s;
        }

        return null;
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[^0-9+]", "");
    }
}
