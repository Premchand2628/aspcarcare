package com.carwash.bookingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)  // ✅ IMPORTANT for rescheduling
@Entity
@Table(name = "carwash_booking")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;

    @Column(name = "booking_date")
    private LocalDate bookingDate;

    private String timeSlot;            // "10:00 - 11:00"
    private String carType;             // Hatchback / Sedan / SUV
    private String serviceType;         // Self Drive / ASP Care / @Home

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "transaction_id")
    private String transactionId;

    @Builder.Default
    private String status = "PENDING";

    @Builder.Default
    private String paymentStatus = "PENDING";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private String carNumber;
    private Integer carIndex;

    // Refund details
    private String refundStatus;
    private BigDecimal refundAmount;
    private LocalDateTime refundInitiatedAt;

    // Service centre
    private Long serviceCentreId;

    @Column(name = "centre_name")
    private String centreName;

    private String mapsUrl;

    // Pricing
    private BigDecimal originalAmount;
    private BigDecimal payableAmount;
    private BigDecimal discountPercentApplied;
    private Boolean freeApplied;

    @Column(name = "membership_id_used")
    private Long membershipIdUsed;

    // Wash details
    @Column(name = "wash_type")
    private String washType;             // BASIC / FOAM / PREMIUM

    @Column(name = "water_provided")
    private Boolean waterProvided;

    @Column(name = "water_discount_applied")
    private BigDecimal waterDiscountApplied;
    
    @Column(name = "upgrade_status")
    private String upgradeStatus;
    private String upgradedFrom;
    private String upgradedTo;
    
    // ============================================================================
    // ✅ NEW RESCHEDULING FIELDS
    // ============================================================================
    
    /**
     * Rescheduling flag - 'Y' if booking has been rescheduled, 'N' otherwise
     */
    @Column(name = "rescheduled", length = 1)
    @Builder.Default
    private String rescheduled = "N";
    
    /**
     * Number of times this booking has been rescheduled (max 2)
     */
    @Column(name = "rescheduled_count")
    @Builder.Default
    private Integer rescheduledCount = 0;
    
    /**
     * Original booking date (stored when first rescheduled)
     */
    @Column(name = "original_booking_date")
    private LocalDate originalBookingDate;
    
    /**
     * Original time slot (stored when first rescheduled)
     */
    @Column(name = "original_time_slot", length = 20)
    private String originalTimeSlot;
    
    /**
     * Timestamp when booking was last rescheduled
     */
    @Column(name = "rescheduled_at")
    private LocalDateTime rescheduledAt;
    
    /**
     * Reason for rescheduling (optional)
     */
    @Column(name = "rescheduled_reason", length = 500)
    private String rescheduledReason;
    
    // Safety net for non-builder creation
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
        if (paymentStatus == null) {
            paymentStatus = "PENDING";
        }
        // Initialize rescheduling fields
        if (rescheduled == null) {
            rescheduled = "N";
        }
        if (rescheduledCount == null) {
            rescheduledCount = 0;
        }
    }
}//package com.carwash.bookingservice.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder(toBuilder = true)  // ✅ IMPORTANT
//@Entity
//@Table(name = "carwash_booking")
//public class Booking {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String phone;
//
//    @Column(name = "booking_date")
//    private LocalDate bookingDate;
//
//    private String timeSlot;            // "10:00 - 11:00"
//    private String carType;             // Hatchback / Sedan / SUV
//    private String serviceType;         // Self Drive / ASP Care / @Home
//
//    @Column(columnDefinition = "TEXT")
//    private String address;
//
//    @Column(name = "transaction_id")
//    private String transactionId;
//
//    @Builder.Default
//    private String status = "PENDING";
//
//    @Builder.Default
//    private String paymentStatus = "PENDING";
//
//    @Builder.Default
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    private String carNumber;
//    private Integer carIndex;
//
//    // Refund details
//    private String refundStatus;
//    private BigDecimal refundAmount;
//    private LocalDateTime refundInitiatedAt;
//
//    // Service centre
//    private Long serviceCentreId;
//
//    @Column(name = "centre_name")
//    private String centreName;
//
//    private String mapsUrl;
//
//    // Pricing
//    private BigDecimal originalAmount;
//    private BigDecimal payableAmount;
//    private BigDecimal discountPercentApplied;
//    private Boolean freeApplied;
//
//    @Column(name = "membership_id_used")
//    private Long membershipIdUsed;
//
//    // Wash details
//    @Column(name = "wash_type")
//    private String washType;             // BASIC / FOAM / PREMIUM
//
//    @Column(name = "water_provided")
//    private Boolean waterProvided;
//
//    @Column(name = "water_discount_applied")
//    private BigDecimal waterDiscountApplied;
//    @Column(name = "upgrade_status")
//    private String upgradeStatus;
//    private String upgradedFrom;
//    private String upgradedTo;
//    
//    // Safety net for non-builder creation
//    @PrePersist
//    public void prePersist() {
//        if (createdAt == null) {
//            createdAt = LocalDateTime.now();
//        }
//        if (status == null) {
//            status = "PENDING";
//        }
//        if (paymentStatus == null) {
//            paymentStatus = "PENDING";
//        }
//    }
//}
