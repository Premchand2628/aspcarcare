package com.carwash.invitation.controller;

import com.carwash.invitation.dto.*;
import com.carwash.invitation.service.ReferralCouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
