package com.carwash.membership.controller;

import com.carwash.membership.dto.DealPriceBookingCreateRequest;
import com.carwash.membership.dto.DealPriceBookingRedeemRequest;
import com.carwash.membership.entity.DealPriceBooking;
import com.carwash.membership.service.DealPriceBookingService;
import com.carwashcommon.security.JwtUserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/memberships/deal-price-bookings")
@CrossOrigin(origins = "*")
public class DealPriceBookingController {

  private final DealPriceBookingService dealPriceBookingService;

  public DealPriceBookingController(DealPriceBookingService dealPriceBookingService) {
    this.dealPriceBookingService = dealPriceBookingService;
  }

  @PostMapping
  public ResponseEntity<?> createBooking(@Valid @RequestBody DealPriceBookingCreateRequest request,
                                         Authentication authentication) {
    try {
      String resolvedPhone = resolvePhone(authentication);
      if (resolvedPhone == null || resolvedPhone.isBlank()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
      }
      DealPriceBooking saved = dealPriceBookingService.createBooking(request, resolvedPhone);
      return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/me")
  public ResponseEntity<?> getMyBookings(Authentication authentication) {
    String resolvedPhone = resolvePhone(authentication);
    if (resolvedPhone == null || resolvedPhone.isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }
    List<DealPriceBooking> bookings = dealPriceBookingService.getBookingsForPhone(resolvedPhone);
    return ResponseEntity.ok(bookings);
  }

  @GetMapping
  public ResponseEntity<?> getBookings(Authentication authentication) {
    return getMyBookings(authentication);
  }

  @GetMapping("/by-phone")
  public ResponseEntity<?> getBookingsByPhone(@RequestParam String phone,
                                              Authentication authentication) {
    String resolvedPhone = resolvePhone(authentication);
    if (resolvedPhone == null || resolvedPhone.isBlank()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }

    if (phone == null || phone.isBlank()) {
      return ResponseEntity.badRequest().body("Phone is required");
    }

    if (!resolvedPhone.equals(phone.trim())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
    }

    List<DealPriceBooking> bookings = dealPriceBookingService.getBookingsForPhone(resolvedPhone);
    return ResponseEntity.ok(bookings);
  }

  @PostMapping("/redeem")
  public ResponseEntity<?> redeemSubscription(@Valid @RequestBody DealPriceBookingRedeemRequest request) {
    try {
      DealPriceBooking updated = dealPriceBookingService.redeemForBooking(request);
      return ResponseEntity.ok(Map.of(
          "success", true,
          "dealPriceBookingId", updated.getId(),
          "leftWashes", updated.getLeftWashes(),
          "usedWashes", updated.getUsedWashes(),
          "planTypeCode", updated.getPlanTypeCode()
      ));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of(
          "success", false,
          "message", e.getMessage()
      ));
    }
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

      return null;
    }

    if (principal instanceof String s && !s.contains("@")) {
      return s;
    }

    return null;
  }
}
