package com.carwash.bookingservice.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.carwash.bookingservice.dto.ApiResponse;
import com.carwash.bookingservice.dto.BookingRequest;
import com.carwash.bookingservice.dto.RefundQuoteResponse;
import com.carwash.bookingservice.dto.StatusUpdateRequest;
import com.carwash.bookingservice.dto.UpdateBookingRequest;
import com.carwash.bookingservice.dto.UpgradeBookingRequest;
import com.carwash.bookingservice.entity.Booking;

/**
 * Service layer for managing car wash bookings
 * 
 * Supports:
 * - Availability checking
 * - Booking creation and confirmation
 * - Payment processing
 * - Status updates and upgrades
 * - Booking updates and rescheduling (with limit of 2)
 * - Cancellation and refund processing
 */
public interface BookingService {

    // ==========================================================
    // READ Operations
    // ==========================================================
    
    /**
     * Get available time slots for a specific date and service type
     * @param date Date in YYYY-MM-DD format
     * @param serviceType Type of service (HOME, ASP_CARE, SELF_DRIVE, etc.)
     * @return Map of time slots with availability status
     */
    Map<String, Boolean> getAvailability(String date, String serviceType);
    
    /**
     * Get all bookings for a specific phone number
     * @param phone Customer's phone number
     * @return List of bookings ordered by creation date (descending)
     */
    List<Booking> getBookingsByPhone(String phone);
    
    /**
     * Get all bookings for a specific email address
     * Looks up the user by email, gets their phone, then fetches bookings by phone
     * @param email Customer's email address
     * @return List of bookings ordered by creation date (descending)
     */
    List<Booking> getBookingsByEmail(String email);
    
    /**
     * Get all bookings in the system
     * @return List of all bookings ordered by creation date (descending)
     */
    List<Booking> getAllBookings();
    
    /**
     * Get a specific booking by ID
     * @param id Booking ID
     * @return Optional containing the booking if found
     */
    Optional<Booking> getBookingById(Long id);

    // ==========================================================
    // CANCEL / REFUND Operations
    // ==========================================================
    
    /**
     * Get refund quote for cancelling a booking
     * @param id Booking ID
     * @return RefundQuoteResponse with eligibility and refund details
     */
    RefundQuoteResponse getCancelQuote(Long id);
    
    /**
     * Confirm cancellation and process refund
     * @param id Booking ID
     * @return ApiResponse with cancellation status and refund details
     */
    ApiResponse cancelConfirm(Long id);

    // ==========================================================
    // CREATE / CONFIRM Operations
    // ==========================================================
    
    /**
     * Create a new booking
     * @param req BookingRequest with booking details
     * @return ApiResponse with booking creation status and ID
     */
    ApiResponse createBooking(BookingRequest req);
    
    /**
     * Confirm multiple bookings in a single order
     * @param requests List of BookingRequest objects
     * @return ApiResponse with confirmation status and booking IDs
     */
    ApiResponse confirmOrder(List<BookingRequest> requests);
    
    /**
     * Mark payment as successful for a booking
     * @param id Booking ID
     * @return ApiResponse with payment confirmation status
     */
    ApiResponse markPaymentSuccess(Long id);

    // ==========================================================
    // UPDATE Operations
    // ==========================================================
    
    /**
     * Update booking status
     * @param id Booking ID
     * @param request StatusUpdateRequest with new status
     * @return Updated Booking entity
     */
    Booking updateBookingStatus(Long id, StatusUpdateRequest request);
    
    /**
     * Upgrade booking wash type (BASIC → FOAM → PREMIUM)
     * @param id Booking ID
     * @param request UpgradeBookingRequest with target wash type
     * @return ApiResponse with upgrade status
     */
    ApiResponse upgradeBooking(Long id, UpgradeBookingRequest request);
    
    /**
     * Update booking date/time or reschedule
     * 
     * Supports two modes:
     * 1. Regular Update: Just change date/time without rescheduling flag
     * 2. Reschedule: Set action="RESCHEDULE" to track reschedule count (max 2)
     * 
     * @param id Booking ID
     * @param request UpdateBookingRequest with new date, time, and optional action
     * @return ApiResponse with update/reschedule status
     * @throws NoSuchElementException if booking not found
     * @throws IllegalStateException if reschedule limit exceeded or invalid status
     * @throws IllegalArgumentException if invalid date format or unavailable slot
     */
    ApiResponse updateBooking(Long id, UpdateBookingRequest request);
}//package com.carwash.bookingservice.service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import com.carwash.bookingservice.dto.ApiResponse;
//import com.carwash.bookingservice.dto.BookingRequest;
//import com.carwash.bookingservice.dto.RefundQuoteResponse;
//import com.carwash.bookingservice.dto.StatusUpdateRequest;
//import com.carwash.bookingservice.dto.UpdateBookingRequest;
//import com.carwash.bookingservice.dto.UpgradeBookingRequest;
//import com.carwash.bookingservice.entity.Booking;
//
//public interface BookingService {
//
//    // READ
//    Map<String, Boolean> getAvailability(String date, String serviceType);
//    List<Booking> getBookingsByPhone(String phone);
//    List<Booking> getAllBookings();
//    Optional<Booking> getBookingById(Long id);
//
//    // CANCEL / REFUND
//    RefundQuoteResponse getCancelQuote(Long id);
//    ApiResponse cancelConfirm(Long id);
//
//    // CREATE / CONFIRM
//    ApiResponse createBooking(BookingRequest req);
//    ApiResponse confirmOrder(List<BookingRequest> requests);
//    ApiResponse markPaymentSuccess(Long id);
//
//    // UPDATE
//    Booking updateBookingStatus(Long id, StatusUpdateRequest request);
//    ApiResponse upgradeBooking(Long id, UpgradeBookingRequest request);
//    ApiResponse updateBooking(Long id, UpdateBookingRequest request);
//}
