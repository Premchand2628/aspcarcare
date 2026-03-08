package com.carwash.bookingservice.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.MDC;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.carwash.bookingservice.dto.ApiResponse;
import com.carwash.bookingservice.dto.BookingRequest;
import com.carwash.bookingservice.dto.RefundQuoteResponse;
import com.carwash.bookingservice.dto.StatusUpdateRequest;
import com.carwash.bookingservice.dto.UpdateBookingRequest;
import com.carwash.bookingservice.dto.UpgradeBookingRequest;
import com.carwash.bookingservice.entity.Booking;
import com.carwash.bookingservice.service.BookingService;
import com.carwash.otplogin.entity.User;
import com.carwash.otplogin.repository.UserRepository;
import com.carwashcommon.security.JwtUserPrincipal;

import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    // ==========================================================
    // AVAILABILITY
    // ==========================================================
    @GetMapping("/availability")
    public ResponseEntity<?> getAvailability(@RequestParam String date,
                                            @RequestParam(required = false, defaultValue = "HOME") String serviceType) {
        try {
            MDC.put("date", date);
            MDC.put("serviceType", serviceType);

            Map<String, Boolean> resp = bookingService.getAvailability(date, serviceType);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            MDC.put("result", "FAILED");
            MDC.put("error", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid date format. Use YYYY-MM-DD"));
        }
    }
    
    // ==========================================================
    // Upgrade booking
    // ==========================================================
    @PutMapping("/{id:\\d+}/upgrade")
    public ResponseEntity<?> upgradeBooking(@PathVariable Long id,
                                            @RequestBody UpgradeBookingRequest request) {
        try {
            ApiResponse resp = bookingService.upgradeBooking(id, request);
            return ResponseEntity.ok(resp);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Booking not found"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Upgrade failed"));
        }
    }
    
    // ==========================================================
    // Update / Reschedule booking
    // ==========================================================
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, 
                                          @RequestBody UpdateBookingRequest request) {
        try {
            ApiResponse response = bookingService.updateBooking(id, request);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Booking not found"));
        } catch (IllegalStateException e) {
            // Check if it's a reschedule limit error (return 409)
            if (e.getMessage().contains("reschedule limit")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse(false, e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update booking"));
        }
    }
    
    // ==========================================================
    // READ
    // ==========================================================
    @GetMapping("/by-phone")
    public ResponseEntity<?> getBookingsByPhone(@RequestParam String phone, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Unauthorized: JWT token required"));
        }

        String resolvedPhone = resolvePhone(authentication);
        if (resolvedPhone == null || resolvedPhone.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Unauthorized: phone not found"));
        }

        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Phone is required"));
        }

        if (!resolvedPhone.equals(phone.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Forbidden"));
        }

        return ResponseEntity.ok(bookingService.getBookingsByPhone(resolvedPhone));
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getBookingsByEmail(@RequestParam String email, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Unauthorized: JWT token required"));
        }

        String resolvedEmail = resolveEmail(authentication);
        if (resolvedEmail == null || resolvedEmail.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Forbidden"));
        }

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Email is required"));
        }

        if (!resolvedEmail.equalsIgnoreCase(email.trim())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Forbidden"));
        }

        return ResponseEntity.ok(bookingService.getBookingsByEmail(resolvedEmail));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getBookingsForMe(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Unauthorized: JWT token required"));
        }

        String resolvedPhone = resolvePhone(authentication);
        if (resolvedPhone == null || resolvedPhone.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Phone number not available"));
        }

        return ResponseEntity.ok(bookingService.getBookingsByPhone(resolvedPhone));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Booking not found")));
    }

    // ==========================================================
    // CANCEL QUOTE
    // ==========================================================
    @GetMapping("/{id:\\d+}/cancel-quote")
    public ResponseEntity<?> getCancelQuote(@PathVariable Long id) {
        try {
            RefundQuoteResponse resp = bookingService.getCancelQuote(id);
            return ResponseEntity.ok(resp);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Booking not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Unable to generate cancel quote"));
        }
    }

    // ==========================================================
    // CANCEL CONFIRM
    // ==========================================================
    @PostMapping("/{id:\\d+}/cancel-confirm")
    public ResponseEntity<?> cancelConfirm(@PathVariable Long id) {
        try {
            ApiResponse resp = bookingService.cancelConfirm(id);
            return ResponseEntity.ok(resp);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Booking not found"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Cancel failed"));
        }
    }

    // ==========================================================
    // CREATE SINGLE BOOKING
    // ==========================================================
    @PostMapping
    public ResponseEntity<ApiResponse> createBooking(@Valid @RequestBody BookingRequest req,
                                                     Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Unauthorized: JWT token required"));
            }

            String resolvedPhone = resolvePhone(authentication);
            if (resolvedPhone == null || resolvedPhone.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Unauthorized: phone not found"));
            }

            req.setPhone(resolvedPhone);
            return ResponseEntity.ok(bookingService.createBooking(req));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Create booking failed"));
        }
    }

    // ==========================================================
    // CONFIRM ORDER (MULTI)
    // ==========================================================
    @PostMapping("/confirm-order")
    public ResponseEntity<ApiResponse> confirmOrder(@RequestBody List<@Valid BookingRequest> requests,
                                                    Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Unauthorized: JWT token required"));
            }

            String resolvedPhone = resolvePhone(authentication);
            if (resolvedPhone == null || resolvedPhone.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Unauthorized: phone not found"));
            }

            if (requests == null || requests.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "No bookings provided"));
            }

            requests.forEach(request -> request.setPhone(resolvedPhone));
            return ResponseEntity.ok(bookingService.confirmOrder(requests));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Confirm order failed"));
        }
    }

    // ==========================================================
    // PAYMENT SUCCESS
    // ==========================================================
    @PutMapping("/{id:\\d+}/payment-success")
    public ResponseEntity<ApiResponse> paymentSuccess(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bookingService.markPaymentSuccess(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Booking not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Payment update failed"));
        }
    }

    // ==========================================================
    // STATUS UPDATE
    // ==========================================================
    @PutMapping("/{id:\\d+}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        try {
            Booking updated = bookingService.updateBookingStatus(id, req);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Booking not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Status update failed"));
        }
    }

    private String resolvePhone(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            Claims claims = jwtUserPrincipal.getClaims();
            Object phoneClaim = claims != null ? claims.get("phone") : null;

            if (phoneClaim != null && !phoneClaim.toString().isBlank()) {
                return phoneClaim.toString();
            }

            String subject = jwtUserPrincipal.getPhone();
            if (subject != null && subject.contains("@")) {
                return userRepository.findByEmail(subject)
                        .map(User::getPhone)
                        .orElse(null);
            }

            return subject;
        }

        if (principal instanceof String s) {
            return s;
        }

        return null;
    }

    private String resolveEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtUserPrincipal) {
            String subject = jwtUserPrincipal.getPhone();
            if (subject != null && subject.contains("@")) {
                return subject;
            }
        }

        if (principal instanceof String s && s.contains("@")) {
            return s;
        }

        return null;
    }
}
//package com.carwash.bookingservice.controller;
//
//import java.util.List;
//import java.util.Map;
//import java.util.NoSuchElementException;
//
//import org.slf4j.MDC;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import com.carwash.bookingservice.dto.ApiResponse;
//import com.carwash.bookingservice.dto.BookingRequest;
//import com.carwash.bookingservice.dto.RefundQuoteResponse;
//import com.carwash.bookingservice.dto.StatusUpdateRequest;
//import com.carwash.bookingservice.dto.UpdateBookingRequest;
//import com.carwash.bookingservice.dto.UpgradeBookingRequest;
//import com.carwash.bookingservice.entity.Booking;
//import com.carwash.bookingservice.service.BookingService;
//
//@RestController
//@RequestMapping("/bookings")
//@CrossOrigin(origins = "*")
//public class BookingController {
//
//    private final BookingService bookingService;
//
//    public BookingController(BookingService bookingService) {
//        this.bookingService = bookingService;
//    }
//
//    // ==========================================================
//    // AVAILABILITY
//    // ==========================================================
//    @GetMapping("/availability")
//    public ResponseEntity<?> getAvailability(@RequestParam String date,
//                                            @RequestParam(required = false, defaultValue = "HOME") String serviceType) {
//        try {
//            MDC.put("date", date);
//            MDC.put("serviceType", serviceType);
//
//            Map<String, Boolean> resp = bookingService.getAvailability(date, serviceType);
//            return ResponseEntity.ok(resp);
//
//        } catch (Exception e) {
//            MDC.put("result", "FAILED");
//            MDC.put("error", e.getClass().getSimpleName());
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid date format. Use YYYY-MM-DD"));
//        }
//    }
//    // ==========================================================
//    // Upgrade booking
//    // ==========================================================
// // BookingController.java
//    @PutMapping("/{id:\\d+}/upgrade")
//    public ResponseEntity<?> upgradeBooking(@PathVariable Long id,
//                                            @RequestBody UpgradeBookingRequest request) {
//        try {
//            ApiResponse resp = bookingService.upgradeBooking(id, request);
//            return ResponseEntity.ok(resp);
//        } catch (NoSuchElementException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ApiResponse(false, "Booking not found"));
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, "Upgrade failed"));
//        }
//    }
//    // ==========================================================
//    // Update booking
//    // ==========================================================
//    @PutMapping("/{id:\\d+}")
//    public ResponseEntity<?> updateBooking(@PathVariable Long id, 
//                                          @RequestBody UpdateBookingRequest request) {
//        try {
//            ApiResponse response = bookingService.updateBooking(id, request);
//            return ResponseEntity.ok(response);
//        } catch (NoSuchElementException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ApiResponse(false, "Booking not found"));
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse(false, "Failed to update booking"));
//        }
//    }
//    // ==========================================================
//    // READ
//    // ==========================================================
//    @GetMapping("/by-phone")
//    public ResponseEntity<?> getBookingsByPhone(@RequestParam String phone) {
//        if (phone == null || phone.isBlank()) {
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Phone is required"));
//        }
//        return ResponseEntity.ok(bookingService.getBookingsByPhone(phone));
//    }
//
//    @GetMapping("/all")
//    public ResponseEntity<List<Booking>> getAllBookings() {
//        return ResponseEntity.ok(bookingService.getAllBookings());
//    }
//
//    @GetMapping("/{id:\\d+}")
//    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
//        return bookingService.getBookingById(id)
//                .<ResponseEntity<?>>map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(new ApiResponse(false, "Booking not found")));
//    }
//
//    // ==========================================================
//    // CANCEL QUOTE
//    // ==========================================================
//    @GetMapping("/{id:\\d+}/cancel-quote")
//    public ResponseEntity<?> getCancelQuote(@PathVariable Long id) {
//        try {
//            RefundQuoteResponse resp = bookingService.getCancelQuote(id);
//            return ResponseEntity.ok(resp);
//        } catch (NoSuchElementException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Booking not found"));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Unable to generate cancel quote"));
//        }
//    }
//
//    // ==========================================================
//    // CANCEL CONFIRM
//    // ==========================================================
//    @PostMapping("/{id:\\d+}/cancel-confirm")
//    public ResponseEntity<?> cancelConfirm(@PathVariable Long id) {
//        try {
//            ApiResponse resp = bookingService.cancelConfirm(id);
//            return ResponseEntity.ok(resp);
//        } catch (NoSuchElementException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Booking not found"));
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Cancel failed"));
//        }
//    }
//
//    // ==========================================================
//    // CREATE SINGLE BOOKING
//    // ==========================================================
//    @PostMapping
//    public ResponseEntity<ApiResponse> createBooking(@RequestBody BookingRequest req) {
//        try {
//            return ResponseEntity.ok(bookingService.createBooking(req));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse(false, "Create booking failed"));
//        }
//    }
//
//    // ==========================================================
//    // CONFIRM ORDER (MULTI)
//    // ==========================================================
//    @PostMapping("/confirm-order")
//    public ResponseEntity<ApiResponse> confirmOrder(@RequestBody List<BookingRequest> requests) {
//        try {
//            return ResponseEntity.ok(bookingService.confirmOrder(requests));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse(false, "Confirm order failed"));
//        }
//    }
//
//    // ==========================================================
//    // PAYMENT SUCCESS
//    // ==========================================================
//    @PutMapping("/{id:\\d+}/payment-success")
//    public ResponseEntity<ApiResponse> paymentSuccess(@PathVariable Long id) {
//        try {
//            return ResponseEntity.ok(bookingService.markPaymentSuccess(id));
//        } catch (NoSuchElementException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Booking not found"));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Payment update failed"));
//        }
//    }
//
//    // ==========================================================
//    // STATUS UPDATE
//    // ==========================================================
//    @PutMapping("/{id:\\d+}/status")
//    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req) {
//        try {
//            Booking updated = bookingService.updateBookingStatus(id, req);
//            return ResponseEntity.ok(updated);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
//        } catch (NoSuchElementException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Booking not found"));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ApiResponse(false, "Status update failed"));
//        }
//    }
//}
//
