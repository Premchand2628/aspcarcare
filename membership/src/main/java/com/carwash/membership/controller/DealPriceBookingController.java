package com.carwash.membership.controller;

import com.carwash.membership.dto.DealPriceBookingCreateRequest;
import com.carwash.membership.entity.DealPriceBooking;
import com.carwash.membership.service.DealPriceBookingService;
import com.carwashcommon.security.JwtUserPrincipal;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/memberships/deal-price-bookings")
@CrossOrigin(origins = "*")
public class DealPriceBookingController {

  private final DealPriceBookingService dealPriceBookingService;

  public DealPriceBookingController(DealPriceBookingService dealPriceBookingService) {
    this.dealPriceBookingService = dealPriceBookingService;
  }

  @PostMapping
  public ResponseEntity<?> createBooking(@RequestBody DealPriceBookingCreateRequest request,
                                         Authentication authentication) {
    try {
      String resolvedPhone = resolvePhone(authentication);
      DealPriceBooking saved = dealPriceBookingService.createBooking(request, resolvedPhone);
      return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
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
