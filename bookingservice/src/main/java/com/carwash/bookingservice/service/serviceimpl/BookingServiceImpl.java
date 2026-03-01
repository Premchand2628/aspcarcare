package com.carwash.bookingservice.service.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.carwash.bookingservice.client.MembershipClient.MembershipClient;
import com.carwash.bookingservice.client.MembershipClient.RateClient;
import com.carwash.bookingservice.dto.ApiResponse;
import com.carwash.bookingservice.dto.BookingRequest;
import com.carwash.bookingservice.dto.RefundQuoteResponse;
import com.carwash.bookingservice.dto.StatusUpdateRequest;
import com.carwash.bookingservice.dto.UpdateBookingRequest;
import com.carwash.bookingservice.dto.UpgradeBookingRequest;
import com.carwash.bookingservice.entity.Booking;
import com.carwash.bookingservice.entity.Refund;
import com.carwash.bookingservice.repository.BookingRepository;
import com.carwash.bookingservice.repository.RefundRepository;
import com.carwash.bookingservice.service.BookingService;
import com.carwash.mailnotification.dto.EmailRequest;
import com.carwash.mailnotification.service.EmailService;
import com.carwash.otplogin.entity.User;
import com.carwash.otplogin.repository.UserRepository;


@Service
public class BookingServiceImpl implements BookingService {

	
    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private static final DateTimeFormatter SLOT_TIME_FMT = DateTimeFormatter.ofPattern("H:mm");

    private static final BigDecimal WATER_DISCOUNT_PCT = new BigDecimal("15");
    private static final int MONEY_SCALE = 2;

    private final BookingRepository bookingRepository;
    private final RefundRepository refundRepository;
    private final MembershipClient membershipClient;
    private final RateClient rateClient;
    private final EmailService emailService;
    private final UserRepository userRepository;

    private static final List<String> TIME_SLOTS = List.of(
            "08:00-09:00","09:00-10:00","10:00-11:00","11:00-12:00",
            "12:00-13:00","13:00-14:00","14:00-15:00","15:00-16:00",
            "16:00-17:00","17:00-18:00","18:00-19:00"
    );

    public BookingServiceImpl(BookingRepository bookingRepository,
                          RefundRepository refundRepository,
                          MembershipClient membershipClient,
                          RateClient rateClient,
                          EmailService emailService,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.refundRepository = refundRepository;
        this.membershipClient = membershipClient;
        this.rateClient = rateClient;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    // ==========================================================
    // READ APIs
    // ==========================================================
    @Override
    public Map<String, Boolean> getAvailability(String date, String serviceType) {
        LocalDate d = LocalDate.parse(date);
        String st = normServiceType(serviceType);

        Map<String, Boolean> slotMap = new LinkedHashMap<>();
        for (String s : TIME_SLOTS) slotMap.put(s, true);

        List<Booking> list = bookingRepository.findByBookingDateAndServiceType(d, st);
        for (Booking b : (list == null ? Collections.<Booking>emptyList() : list)) {
            String slot = b.getTimeSlot();
            if (slot == null) continue;

            String status = upper(b.getStatus());
            if (!status.equals("CANCELLED") && !status.equals("CLOSED")) {
                if (slotMap.containsKey(slot)) slotMap.put(slot, false);
            }
        }
        return slotMap;
    }
    @Override
    public List<Booking> getBookingsByPhone(String phone) {
        return bookingRepository.findByPhoneOrderByCreatedAtDesc(phone.trim());
    }
    
    @Override
    public List<Booking> getBookingsByEmail(String email) {
        // Find user by email to get their phone number
        Optional<User> user = userRepository.findByEmail(email.trim());
        if (user.isEmpty()) {
            return Collections.emptyList();
        }
        // Get bookings by the user's phone number
        String phone = user.get().getPhone();
        return bookingRepository.findByPhoneOrderByCreatedAtDesc(phone);
    }
    
    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }
    @Override
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    // ==========================================================
    // CANCEL / REFUND
    // ==========================================================
    @Override
    public RefundQuoteResponse getCancelQuote(Long id) {
        Booking b = bookingRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Booking not found"));

        String status = upper(b.getStatus());
        String pay = upper(b.getPaymentStatus());

        if (status.equals("CANCELLED") || status.equals("IN_SERVICING") || status.equals("COMPLETED")) {
            RefundQuoteResponse r = new RefundQuoteResponse();
            r.setEligible(false);
            r.setMessage("This booking cannot be cancelled in current status: " + status);
            return r;
        }

        if (!(pay.equals("SUCCESS") || pay.equals("PAID"))) {
            RefundQuoteResponse r = new RefundQuoteResponse();
            r.setEligible(false);
            r.setMessage("Only PAID bookings can be cancelled from here.");
            return r;
        }

        LocalDateTime slotStart = resolveSlotStart(b.getBookingDate(), b.getTimeSlot());
        double hrs = Math.max(0, computeHoursRemaining(slotStart));
        double pct = refundPercentForHours(hrs);

        BigDecimal paid = paidAmountForRefund(b);
        BigDecimal refund = paid.multiply(BigDecimal.valueOf(pct))
                .divide(BigDecimal.valueOf(100), MONEY_SCALE, RoundingMode.HALF_UP);

        RefundQuoteResponse resp = new RefundQuoteResponse();
        resp.setEligible(true);
        resp.setMessage(pct == 0 ? "Less than 6 hours left. Cancellation allowed but refund is ₹0." : "Cancellation eligible.");
        resp.setHoursRemaining(hrs);
        resp.setRefundPercent(pct);
        resp.setBookingAmount(paid.doubleValue());
        resp.setRefundAmount(refund.doubleValue());
        return resp;
    }
    @Override
    public ApiResponse cancelConfirm(Long id) {
        Booking b = bookingRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Booking not found"));

        String status = upper(b.getStatus());
        String pay = upper(b.getPaymentStatus());

        if (status.equals("CANCELLED")) return new ApiResponse(true, "Booking already cancelled.");
        if (status.equals("IN_SERVICING") || status.equals("COMPLETED")) {
            throw new IllegalStateException("Cannot cancel booking in status: " + status);
        }
        if (!(pay.equals("SUCCESS") || pay.equals("PAID"))) {
            throw new IllegalStateException("Only PAID bookings can be cancelled.");
        }

        LocalDateTime slotStart = resolveSlotStart(b.getBookingDate(), b.getTimeSlot());
        double hrs = Math.max(0, computeHoursRemaining(slotStart));
        double pct = refundPercentForHours(hrs);

        BigDecimal paid = paidAmountForRefund(b);
        BigDecimal refundAmt = paid.multiply(BigDecimal.valueOf(pct))
                .divide(BigDecimal.valueOf(100), MONEY_SCALE, RoundingMode.HALF_UP);

        Booking updated = b.toBuilder()
                .status("CANCELLED")
                .refundInitiatedAt(LocalDateTime.now())
                .refundStatus(refundAmt.compareTo(BigDecimal.ZERO) > 0 ? "INITIATED" : "NO_REFUND")
                .refundAmount(refundAmt.compareTo(BigDecimal.ZERO) > 0 ? refundAmt : BigDecimal.ZERO)
                .build();

        bookingRepository.save(updated);

        Refund r = Refund.builder()
                .bookingId(updated.getId())
                .phone(updated.getPhone())
                .carNumber(updated.getCarNumber())
                .carType(updated.getCarType())
                .serviceType(updated.getServiceType())
                .address(updated.getAddress())
                .bookingDate(updated.getBookingDate())
                .timeSlot(updated.getTimeSlot())
                .carIndex(updated.getCarIndex().toString())
                .bookingAmount(paid)
                .refundPercent(BigDecimal.valueOf(pct))
                .refundAmount(refundAmt)
                .hoursRemaining(hrs)
                .refundStatus(refundAmt.compareTo(BigDecimal.ZERO) > 0 ? "INITIATED" : "NO_REFUND")
                .initiatedAt(LocalDateTime.now())
                .build();

        refundRepository.save(r);
        try {
            sendCancellationEmail(updated);
        } catch (Exception ex) {
            log.warn("Failed to send cancellation email for booking ID: {}", updated.getId(), ex);
        }
        String msg = (refundAmt.compareTo(BigDecimal.ZERO) > 0)
                ? ("Booking cancelled. Refund initiated: ₹" + refundAmt + " (" + pct + "%).")
                : "Booking cancelled. No refund (less than 6 hours).";

        return new ApiResponse(true, msg);
    }

    // ==========================================================
    // CREATE / CONFIRM
    // ==========================================================
    @Override
    public ApiResponse createBooking(BookingRequest req) {
        String error = validateBookingRequest(req);
        if (error != null) throw new IllegalArgumentException(error);

        if (!isBlank(req.getTransactionId())) {
            boolean exists = bookingRepository.existsByTransactionId(req.getTransactionId().trim());
            if (exists) throw new IllegalArgumentException("This transaction ID is already used for another booking.");
        }

        BigDecimal original = resolveOriginalAmount(req);

        Booking booking = Booking.builder()
                .phone(req.getPhone())
                .bookingDate(LocalDate.parse(req.getDate()))
                .timeSlot(req.getTimeslot())
                .carType(req.getCarType())
                .serviceType(normServiceType(req.getServiceType()))
                .washType(Optional.ofNullable(req.getWashType()).orElse("BASIC"))
                .address(Optional.ofNullable(req.getAddress()).orElse("NA"))
                .transactionId(isBlank(req.getTransactionId()) ? null : req.getTransactionId().trim())
                .status("PENDING")
                .paymentStatus("PENDING")
                .carNumber(req.getCarNumber())
                .carIndex(req.getCarIndex() == null ? 0 : req.getCarIndex())
                .serviceCentreId(fixCentreIdForHome(req.getServiceType(), req.getServiceCentreId()))
                .centreName(fixCentreNameForHome(req.getServiceType(), req.getCentreName()))
                .mapsUrl(req.getMapsUrl())
                .originalAmount(original)
                .payableAmount(original)
                .discountPercentApplied(BigDecimal.ZERO)
                .freeApplied(false)
                .membershipIdUsed(null)
                .waterProvided(Boolean.TRUE.equals(req.getWaterProvided()))
                .waterDiscountApplied(BigDecimal.ZERO)
                .refundStatus(null)
                .refundAmount(null)
                .refundInitiatedAt(null)
                .build();

        // membership preview
        try {
            Map<String, Object> benefit = membershipClient.preview(req.getPhone(), original, booking.getWashType());
            booking = applyMembershipPreviewToBooking(booking, original, benefit);
        } catch (Exception ex) {
            log.warn("Membership preview failed. err={}", ex.getMessage());
        }

        // water discount
        booking = applyWaterDiscountBuilder(booking);

        Booking saved = bookingRepository.save(booking);
        try {
            sendBookingConfirmationEmail(saved, req.getPhone());
        } catch (Exception ex) {
            log.warn("Failed to send booking email for booking ID: {}", saved.getId(), ex);
        }
        return new ApiResponse(true, "Booking created. Proceed to payment.", String.valueOf(saved.getId()));
    }
    @Override
    public ApiResponse confirmOrder(List<BookingRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("At least one booking is required");
        }

        List<String> errors = new ArrayList<>();
        List<Booking> toSave = new ArrayList<>();
        BigDecimal orderTotalPayable = BigDecimal.ZERO;

        for (int i = 0; i < requests.size(); i++) {
            BookingRequest req = requests.get(i);

            String err = validateBookingRequest(req);
            if (err != null) {
                errors.add("Item " + (i + 1) + ": " + err);
                continue;
            }

            BigDecimal original = resolveOriginalAmount(req);

            Booking booking = Booking.builder()
                    .phone(req.getPhone())
                    .bookingDate(LocalDate.parse(req.getDate()))
                    .timeSlot(req.getTimeslot())
                    .carType(req.getCarType())
                    .serviceType(normServiceType(req.getServiceType()))
                    .washType(Optional.ofNullable(req.getWashType()).orElse("BASIC"))
                    .address(Optional.ofNullable(req.getAddress()).orElse("NA"))
                    .transactionId(null) // set later after validation
                    .status("PENDING")
                    .paymentStatus("PENDING")
                    .carNumber(req.getCarNumber())
                    .carIndex(req.getCarIndex() == null ? 0 : req.getCarIndex())
                    .serviceCentreId(fixCentreIdForHome(req.getServiceType(), req.getServiceCentreId()))
                    .centreName(fixCentreNameForHome(req.getServiceType(), req.getCentreName()))
                    .mapsUrl(req.getMapsUrl())
                    .originalAmount(original)
                    .payableAmount(original)
                    .discountPercentApplied(BigDecimal.ZERO)
                    .freeApplied(false)
                    .membershipIdUsed(null)
                    .waterProvided(Boolean.TRUE.equals(req.getWaterProvided()))
                    .waterDiscountApplied(BigDecimal.ZERO)
                    .build();

            // membership preview
            try {
                Map<String, Object> benefit = membershipClient.preview(req.getPhone(), original, booking.getWashType());
                booking = applyMembershipPreviewToBooking(booking, original, benefit);
            } catch (Exception ex) {
                log.warn("Membership preview failed. err={}", ex.getMessage());
            }

            // water discount
            booking = applyWaterDiscountBuilder(booking);

            BigDecimal p = booking.getPayableAmount() == null ? BigDecimal.ZERO : booking.getPayableAmount();
            if (p.compareTo(BigDecimal.ZERO) < 0) p = BigDecimal.ZERO;
            orderTotalPayable = orderTotalPayable.add(p);

            toSave.add(booking);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join("; ", errors));
        }

        // txn rules
        String finalTxn = null;

        if (orderTotalPayable.compareTo(BigDecimal.ZERO) > 0) {
            for (BookingRequest req : requests) {
                if (isBlank(req.getTransactionId())) throw new IllegalArgumentException("Transaction ID is required");
                String current = req.getTransactionId().trim();
                if (finalTxn == null) finalTxn = current;
                else if (!finalTxn.equals(current)) throw new IllegalArgumentException("All bookings must have the same Transaction ID");
            }

            boolean duplicate = bookingRepository.existsByTransactionId(finalTxn);
            if (duplicate) {
                throw new IllegalArgumentException("This transaction ID is already used for another booking. Please re-check or contact customer care");
            }

            final String txn = finalTxn;
            toSave = toSave.stream()
                    .map(b -> b.toBuilder().transactionId(txn).build())
                    .collect(Collectors.toList());
        }

        boolean isFreebieOrder = orderTotalPayable.compareTo(BigDecimal.ZERO) == 0;

        List<Booking> saved = bookingRepository.saveAll(toSave);
        String ids = saved.stream().map(b -> String.valueOf(b.getId())).collect(Collectors.joining(","));

        if (isFreebieOrder) {
            return new ApiResponse(true, "Order confirmed successfully (Free booking).", ids);
        }
        return new ApiResponse(true, "Order is on hold. It will take 5 mins to confirm.", ids);
    }
    @Override
    public ApiResponse markPaymentSuccess(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Booking not found"));

        BigDecimal original = booking.getOriginalAmount();
        if (original == null) original = BigDecimal.valueOf(getBaseAmountForCarType(booking.getCarType()));

        Booking updated = booking.toBuilder()
                .paymentStatus("SUCCESS")
                .status("CONFIRMED")
                .originalAmount(original)
                .serviceCentreId(fixCentreIdForHome(booking.getServiceType(), booking.getServiceCentreId()))
                .centreName(fixCentreNameForHome(booking.getServiceType(), booking.getCentreName()))
                .build();

        // membership apply
        try {
            membershipClient.apply(updated.getPhone(), original, updated.getWashType(), updated.getTransactionId());
        } catch (Exception ex) {
            log.warn("Membership apply failed. err={}", ex.getMessage());
        }

        bookingRepository.save(updated);
        return new ApiResponse(true, "Payment confirmed, booking updated");
    }
    @Override
    public Booking updateBookingStatus(Long id, StatusUpdateRequest request) {
        if (request == null || isBlank(request.getStatus())) {
            throw new IllegalArgumentException("Status is required");
        }

        String newStatus = request.getStatus().trim().toUpperCase();
        if (!Set.of("PENDING","IN_SERVICING","COMPLETED","CLOSED","CANCELLED","CONFIRMED").contains(newStatus)) {
            throw new IllegalArgumentException("Invalid status");
        }

        Booking booking = bookingRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Booking not found"));

        Booking updated = booking.toBuilder()
                .status(newStatus)
                .serviceCentreId(fixCentreIdForHome(booking.getServiceType(), booking.getServiceCentreId()))
                .centreName(fixCentreNameForHome(booking.getServiceType(), booking.getCentreName()))
                .build();

        return bookingRepository.save(updated);
    }
 // BookingServiceImpl.java
    @Override
    public ApiResponse upgradeBooking(Long id, UpgradeBookingRequest request) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        String status = upper(b.getStatus());
        if (status.equals("CANCELLED") || status.equals("IN_SERVICING") || status.equals("COMPLETED")) {
            throw new IllegalStateException("Cannot upgrade booking in status: " + status);
        }

        String current = upper(b.getWashType());
        String target = upper(request.getWashType());

        // Allowed upgrades only
        if (current.equals("BASIC") && !(target.equals("FOAM") || target.equals("PREMIUM"))) {
            throw new IllegalStateException("Only Foam or Premium allowed for Basic");
        }
        if (current.equals("FOAM") && !target.equals("PREMIUM")) {
            throw new IllegalStateException("Only Premium allowed for Foam");
        }
        if (current.equals("PREMIUM")) {
            throw new IllegalStateException("No upgrade available for Premium");
        }

        // Update amounts (replace with your rate logic if needed)
        BigDecimal originalAmount = b.getOriginalAmount() != null
                ? b.getOriginalAmount()
                : BigDecimal.ZERO;

        // TODO: replace this with real rate calculation (e.g., from rates table)
        BigDecimal upgradedAmount = originalAmount; 

        BigDecimal waterDiscount = b.getWaterDiscountApplied() != null
                ? b.getWaterDiscountApplied()
                : BigDecimal.ZERO;

        BigDecimal payable = upgradedAmount.subtract(waterDiscount).max(BigDecimal.ZERO);

        Booking updated = b.toBuilder()
                .washType(target)
                .upgradeStatus("UPGRADED")
                .upgradedFrom(current)
                .upgradedTo(target)
                .originalAmount(upgradedAmount)
                .payableAmount(payable)
                .build();

        bookingRepository.save(updated);
        try {
            sendUpgradeEmail(updated, current, target);
        } catch (Exception ex) {
            log.warn("Failed to send upgrade email for booking ID: {}", updated.getId(), ex);
        }
        return new ApiResponse(true, "Booking upgraded successfully");
    }

    @Override
    public ApiResponse updateBooking(Long id, UpdateBookingRequest request) {
        // Find the booking
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));
        
        // ALWAYS treat updates as rescheduling if date/slot is different
        // This auto-tracks reschedules without requiring "action" field
        String currentStatus = upper(booking.getStatus());
        
        if (currentStatus.equals("CANCELLED")) {
            throw new IllegalStateException("Cannot update cancelled booking");
        }
        if (currentStatus.equals("COMPLETED") || currentStatus.equals("IN_SERVICING")) {
            throw new IllegalStateException("Cannot update booking in status: " + currentStatus);
        }
        
        // Parse and validate new date
        LocalDate newDate;
        try {
            newDate = LocalDate.parse(request.getBookingDate());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
        }
        
        if (newDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot update to a past date");
        }
        
        // Validate time slot
        String newSlot = request.getTimeSlot();
        if (newSlot == null || newSlot.trim().isEmpty()) {
            throw new IllegalArgumentException("Time slot is required");
        }
        
        // Check slot availability (exclude current booking)
        String serviceType = booking.getServiceType() != null ? booking.getServiceType() : "HOME";
        if (!isSlotAvailableForReschedule(request.getBookingDate(), newSlot, serviceType, booking.getId())) {
            throw new IllegalStateException("Selected time slot is not available");
        }
        
        // Check reschedule limit
        int currentCount = booking.getRescheduledCount() != null ? booking.getRescheduledCount() : 0;
        if (currentCount >= 2) {
            throw new IllegalStateException("Maximum reschedule limit (2) reached for this booking");
        }
        
        // Store original booking details on FIRST reschedule only
        Booking.BookingBuilder builder = booking.toBuilder();
        
        if (currentCount == 0) {
            builder.originalBookingDate(booking.getBookingDate())
                   .originalTimeSlot(booking.getTimeSlot());
        }
        
        // Update booking with reschedule tracking
        Booking updated = builder
                .bookingDate(newDate)
                .timeSlot(newSlot)
                .rescheduled("Y")
                .rescheduledCount(currentCount + 1)
                .rescheduledAt(LocalDateTime.now())
                .rescheduledReason(request.getRescheduledReason())
                .status("PENDING")  // Reset to PENDING after reschedule
                .build();
        
        bookingRepository.save(updated);
        try {
            sendRescheduleEmail(updated);
        } catch (Exception ex) {
            log.warn("Failed to send reschedule email for booking ID: {}", updated.getId(), ex);
        }
        int remaining = 2 - (currentCount + 1);
        String message = String.format("Booking rescheduled successfully. Reschedules remaining: %d", remaining);
        
        return new ApiResponse(true, message);
    }
    /**
     * Handle rescheduling with limit tracking (max 2 reschedules)
     */
//    private ApiResponse handleRescheduleBooking(Booking booking, UpdateBookingRequest request) {
//        // Validate booking status
//        String status = upper(booking.getStatus());
//        if (status.equals("CANCELLED")) {
//            throw new IllegalStateException("Cannot reschedule cancelled booking");
//        }
//        if (status.equals("COMPLETED") || status.equals("IN_SERVICING")) {
//            throw new IllegalStateException("Cannot reschedule booking in status: " + status);
//        }
//        
//        // Check reschedule limit (MAX = 2)
//        int currentCount = booking.getRescheduledCount() != null ? booking.getRescheduledCount() : 0;
//        if (currentCount >= 2) {
//            throw new IllegalStateException("Maximum reschedule limit (2) reached for this booking");
//        }
//        
//        // Parse and validate new date
//        LocalDate newDate;
//        try {
//            newDate = LocalDate.parse(request.getBookingDate());
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
//        }
//        
//        // Validate date is not in past
//        if (newDate.isBefore(LocalDate.now())) {
//            throw new IllegalArgumentException("Cannot reschedule to a past date");
//        }
//        
//        // Validate time slot
//        String newSlot = request.getTimeSlot();
//        if (newSlot == null || newSlot.trim().isEmpty()) {
//            throw new IllegalArgumentException("Time slot is required");
//        }
//        
//        // Check slot availability (exclude current booking)
//        String serviceType = booking.getServiceType() != null ? booking.getServiceType() : "HOME";
//        if (!isSlotAvailableForReschedule(request.getBookingDate(), newSlot, serviceType, booking.getId())) {
//            throw new IllegalStateException("Selected time slot is not available");
//        }
//        
//        // Store original booking details on FIRST reschedule only
//        Booking.BookingBuilder builder = booking.toBuilder();
//        
//        if (currentCount == 0) {
//            builder.originalBookingDate(booking.getBookingDate())
//                   .originalTimeSlot(booking.getTimeSlot());
//        }
//        
//        // Update booking with reschedule tracking
//        Booking updated = builder
//                .bookingDate(newDate)
//                .timeSlot(newSlot)
//                .rescheduled("Y")
//                .rescheduledCount(currentCount + 1)
//                .rescheduledAt(LocalDateTime.now())
//                .rescheduledReason(request.getRescheduledReason())
//                .status("PENDING")  // Reset to PENDING after reschedule
//                .build();
//        
//        bookingRepository.save(updated);
//        
//        int remaining = 2 - (currentCount + 1);
//        String message = String.format("Booking rescheduled successfully. Reschedules remaining: %d", remaining);
//        
//        return new ApiResponse(true, message);
//    }
//
//    /**
//     * Handle regular booking update without reschedule tracking
//     */
//    private ApiResponse handleRegularBookingUpdate(Booking booking, UpdateBookingRequest request) {
//        // Check if booking can be updated
//        String status = upper(booking.getStatus());
//        if (status.equals("CANCELLED")) {
//            throw new IllegalStateException("Cannot update cancelled booking");
//        }
//        if (status.equals("COMPLETED") || status.equals("IN_SERVICING")) {
//            throw new IllegalStateException("Cannot update booking in status: " + status);
//        }
//        
//        // Parse and validate new date
//        LocalDate newDate;
//        try {
//            newDate = LocalDate.parse(request.getBookingDate());
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
//        }
//        
//        // Validate time slot
//        String newSlot = request.getTimeSlot();
//        if (newSlot == null || newSlot.trim().isEmpty()) {
//            throw new IllegalArgumentException("Time slot is required");
//        }
//        
//        // Check slot availability
//        String serviceType = booking.getServiceType() != null ? booking.getServiceType() : "HOME";
//        Map<String, Boolean> availability = getAvailability(request.getBookingDate(), serviceType);
//        
//        if (availability.containsKey(newSlot) && !availability.get(newSlot)) {
//            throw new IllegalStateException("Selected time slot is not available");
//        }
//        
//        // Simple update without rescheduling tracking
//        Booking updated = booking.toBuilder()
//                .bookingDate(newDate)
//                .timeSlot(newSlot)
//                .build();
//        
//        bookingRepository.save(updated);
//        
//        return new ApiResponse(true, "Booking updated successfully");
//    }

    /**
     * Check if slot is available for rescheduling (excluding current booking)
     */
    private boolean isSlotAvailableForReschedule(String date, String timeSlot, String serviceType, Long bookingId) {
        LocalDate d = LocalDate.parse(date);
        List<Booking> list = bookingRepository.findByBookingDateAndServiceType(d, serviceType);
        
        if (list == null) return true;
        
        for (Booking b : list) {
            // Skip the current booking being rescheduled
            if (b.getId().equals(bookingId)) continue;
            
            String slot = b.getTimeSlot();
            if (slot == null) continue;
            
            String status = upper(b.getStatus());
            if (!status.equals("CANCELLED") && !status.equals("CLOSED")) {
                if (timeSlot.equals(slot)) {
                    return false;
                }
            }
        }
        return true;
    }
//    @Override
//    public ApiResponse updateBooking(Long id, UpdateBookingRequest request) {
//        // Find the booking
//        Booking booking = bookingRepository.findById(id)
//                .orElseThrow(() -> new NoSuchElementException("Booking not found"));
//        
//        // Check if booking can be updated
//        String status = booking.getStatus() != null ? booking.getStatus().toUpperCase() : "";
//        if (status.equals("CANCELLED")) {
//            throw new IllegalStateException("Cannot update cancelled booking");
//        }
//        if (status.equals("COMPLETED") || status.equals("IN_SERVICING")) {
//            throw new IllegalStateException("Cannot update booking in status: " + status);
//        }
//        
//        // Parse and validate new date
//        LocalDate newDate;
//        try {
//            newDate = LocalDate.parse(request.getBookingDate());
//        } catch (Exception e) {
//            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
//        }
//        
//        // Validate time slot format
//        String newSlot = request.getTimeSlot();
//        if (newSlot == null || newSlot.trim().isEmpty()) {
//            throw new IllegalArgumentException("Time slot is required");
//        }
//        
//        // Optional: Check if the new slot is available
//        String serviceType = booking.getServiceType() != null ? 
//                booking.getServiceType() : "HOME";
//        Map<String, Boolean> availability = getAvailability(
//                request.getBookingDate(), serviceType);
//        
//        if (availability.containsKey(newSlot) && !availability.get(newSlot)) {
//            throw new IllegalStateException("Selected time slot is not available");
//        }
//        
//        // Update the booking
//        Booking updated = booking.toBuilder()
//                .bookingDate(newDate)
//                .timeSlot(newSlot)
//                .build();
//        
//        bookingRepository.save(updated);
//        
//        return new ApiResponse(true, "Booking updated successfully");
//    }
    // ==========================================================
    // BUILDER HELPERS
    // ==========================================================
    
    private Booking applyMembershipPreviewToBooking(Booking booking, BigDecimal original, Map<String, Object> benefit) {
        if (booking == null) return booking;

        boolean freeApplied = benefit != null && Boolean.TRUE.equals(benefit.get("freeApplied"));

        BigDecimal payable = original;
        Object payableObj = benefit == null ? null : benefit.get("payableAmount");
        if (payableObj != null) payable = new BigDecimal(String.valueOf(payableObj));

        BigDecimal pct = BigDecimal.ZERO;
        Object pctObj = benefit == null ? null : benefit.get("discountPercent");
        if (pctObj != null) pct = new BigDecimal(String.valueOf(pctObj));

        Long membershipId = null;
        Object midObj = benefit == null ? null : benefit.get("membershipDbId");
        if (midObj != null) {
            String midStr = String.valueOf(midObj).trim();
            if (!midStr.isEmpty() && !"null".equalsIgnoreCase(midStr)) {
                try { membershipId = Long.parseLong(midStr); } catch (Exception ignore) {}
            }
        }

        return booking.toBuilder()
                .payableAmount(payable)
                .freeApplied(freeApplied)
                .discountPercentApplied(pct)
                .membershipIdUsed(membershipId)
                .build();
    }
    
    private Booking applyWaterDiscountBuilder(Booking booking) {
        if (booking == null) return null;

        boolean waterProvided = Boolean.TRUE.equals(booking.getWaterProvided());
        if (!waterProvided) {
            return booking.toBuilder()
                    .waterDiscountApplied(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal payable = booking.getPayableAmount();
        if (payable == null || payable.compareTo(BigDecimal.ZERO) <= 0) {
            return booking.toBuilder()
                    .waterDiscountApplied(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal discount = payable
                .multiply(WATER_DISCOUNT_PCT)
                .divide(new BigDecimal("100"), MONEY_SCALE, RoundingMode.HALF_UP);

        if (discount.compareTo(BigDecimal.ZERO) < 0) discount = BigDecimal.ZERO;

        BigDecimal newPayable = payable.subtract(discount);
        if (newPayable.compareTo(BigDecimal.ZERO) < 0) newPayable = BigDecimal.ZERO;

        return booking.toBuilder()
                .waterDiscountApplied(discount)
                .payableAmount(newPayable)
                .build();
    }

    // ==========================================================
    // HELPERS
    // ==========================================================
    
    private String validateBookingRequest(BookingRequest req) {
        if (req == null) return "Request body is required";
        if (isBlank(req.getPhone())) return "Phone is required";
        if (isBlank(req.getDate())) return "Date is required";
        if (isBlank(req.getTimeslot())) return "Time slot is required";
        if (isBlank(req.getCarType())) return "Car type is required";
        if (isBlank(req.getServiceType())) return "Service type is required";
        if (isBlank(req.getWashType())) return "Wash type is required";
        return null;
    }
    
    private BigDecimal resolveOriginalAmount(BookingRequest req) {
        if (req.getBaseAmount() != null && req.getBaseAmount().compareTo(BigDecimal.ZERO) >= 0) {
            return req.getBaseAmount();
        }

        try {
            return rateClient.getAmount(req.getCarType(), req.getWashType());
        } catch (Exception e) {
            return BigDecimal.valueOf(getBaseAmountForCarType(req.getCarType()));
        }
    }

    private Long fixCentreIdForHome(String serviceType, Long centreId) {
        if (isHomeService(serviceType)) return (centreId == null ? 0L : centreId);
        return centreId;
    }

    private String fixCentreNameForHome(String serviceType, String centreName) {
        if (isHomeService(serviceType)) return (isBlank(centreName) ? "@Home (Doorstep)" : centreName);
        return centreName;
    }

    private boolean isHomeService(String serviceType) {
        if (serviceType == null) return false;
        String s = serviceType.trim().toUpperCase();
        return s.equals("HOME") || s.equals("@HOME") || s.equals("AT_HOME") || s.equals("AT-HOME");
    }

    private String normServiceType(String s) {
        if (s == null) return "HOME";
        s = s.trim().toUpperCase();
        if (s.equals("@HOME") || s.equals("AT_HOME") || s.equals("AT-HOME")) return "HOME";
        return s;
    }

    private LocalDateTime resolveSlotStart(LocalDate bookingDate, String timeSlot) {
        if (bookingDate == null) return null;
        if (isBlank(timeSlot)) return bookingDate.atStartOfDay();
        try {
            String[] parts = timeSlot.split("-");
            String startStr = parts[0].trim();
            LocalTime start = LocalTime.parse(startStr, SLOT_TIME_FMT);
            return bookingDate.atTime(start);
        } catch (Exception e) {
            return bookingDate.atStartOfDay();
        }
    }

    private double computeHoursRemaining(LocalDateTime slotStart) {
        if (slotStart == null) return 0.0;
        LocalDateTime now = LocalDateTime.now();
        long minutes = Duration.between(now, slotStart).toMinutes();
        return minutes / 60.0;
    }

    private double refundPercentForHours(double hrs) {
        if (hrs >= 24.0) return 100.0;
        if (hrs >= 18.0) return 75.0;
        if (hrs >= 6.0)  return 50.0;
        return 0.0;
    }

    private BigDecimal paidAmountForRefund(Booking b) {
        if (b.getPayableAmount() != null) return b.getPayableAmount();
        if (b.getOriginalAmount() != null) return b.getOriginalAmount();
        return BigDecimal.ZERO;
    }

    private double getBaseAmountForCarType(String carType) {
        if (carType == null) return 0.0;
        switch (carType.trim().toUpperCase()) {
            case "HATCHBACK": return 300.0;
            case "SEDAN":     return 400.0;
            case "SUV":       return 500.0;
            case "MPV":       return 600.0;
            case "PICKUP":    return 800.0;
            case "BIKE":      return 200.0;
            default:          return 0.0;
        }
    }

    private String upper(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
 // ========== EMAIL HELPER METHODS ==========

    private void sendBookingConfirmationEmail(Booking booking, String phone) {
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (!userOpt.isPresent()) return;
        
        User user = userOpt.get();
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) return;
        
        EmailRequest emailRequest = EmailRequest.builder()
                .toEmail(user.getEmail())
                .firstName(user.getFirstName())
                .bookingId(booking.getId())
                .washType(booking.getWashType())
                .carNumber(booking.getCarNumber())
                .carType(booking.getCarType())
                .bookingDate(booking.getBookingDate().toString())
                .timeSlot(booking.getTimeSlot())
                .amount(booking.getPayableAmount().doubleValue())
                .action("BOOKED")
                .build();
        
        emailService.sendBookingEmail(emailRequest);
    }

    private void sendRescheduleEmail(Booking booking) {
        Optional<User> userOpt = userRepository.findByPhone(booking.getPhone());
        if (!userOpt.isPresent()) return;
        
        User user = userOpt.get();
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) return;
        
        EmailRequest emailRequest = EmailRequest.builder()
                .toEmail(user.getEmail())
                .firstName(user.getFirstName())
                .bookingId(booking.getId())
                .washType(booking.getWashType())
                .carNumber(booking.getCarNumber())
                .carType(booking.getCarType())
                .bookingDate(booking.getBookingDate().toString())
                .timeSlot(booking.getTimeSlot())
                .amount(booking.getPayableAmount().doubleValue())
                .action("RESCHEDULED")
                .build();
        
        emailService.sendBookingEmail(emailRequest);
    }

    private void sendCancellationEmail(Booking booking) {
        Optional<User> userOpt = userRepository.findByPhone(booking.getPhone());
        if (!userOpt.isPresent()) return;
        
        User user = userOpt.get();
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) return;
        
        EmailRequest emailRequest = EmailRequest.builder()
                .toEmail(user.getEmail())
                .firstName(user.getFirstName())
                .bookingId(booking.getId())
                .washType(booking.getWashType())
                .carNumber(booking.getCarNumber())
                .carType(booking.getCarType())
                .bookingDate(booking.getBookingDate().toString())
                .timeSlot(booking.getTimeSlot())
                .amount(booking.getPayableAmount().doubleValue())
                .action("CANCELLED")
                .build();
        
        emailService.sendBookingEmail(emailRequest);
    }

    private void sendUpgradeEmail(Booking booking, String originalWashType, String upgradedWashType) {
        Optional<User> userOpt = userRepository.findByPhone(booking.getPhone());
        if (!userOpt.isPresent()) return;
        
        User user = userOpt.get();
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) return;
        
        EmailRequest emailRequest = EmailRequest.builder()
                .toEmail(user.getEmail())
                .firstName(user.getFirstName())
                .bookingId(booking.getId())
                .washType(upgradedWashType)
                .carNumber(booking.getCarNumber())
                .carType(booking.getCarType())
                .bookingDate(booking.getBookingDate().toString())
                .timeSlot(booking.getTimeSlot())
                .amount(booking.getPayableAmount().doubleValue())
                .action("UPGRADED")
                .originalWashType(originalWashType)
                .upgradedWashType(upgradedWashType)
                .build();
        
        emailService.sendBookingEmail(emailRequest);
    }
}
