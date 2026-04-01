package com.carwash.bookingservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.carwash.bookingservice.entity.Booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {

    private Long id;
    private String bookingCode;
    private LocalDate bookingDate;
    private String timeSlot;
    private String carType;
    private String serviceType;
    private String washType;
    private String address;
    private String status;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private String carNumber;
    private Integer carIndex;

    private Long serviceCentreId;
    private String centreName;
    private String mapsUrl;

    private BigDecimal originalAmount;
    private BigDecimal payableAmount;
    private BigDecimal discountPercentApplied;
    private Boolean freeApplied;
    private Boolean subscriptionRedeemed;
    private Boolean waterProvided;
    private BigDecimal waterDiscountApplied;

    private String refundStatus;
    private BigDecimal refundAmount;
    private LocalDateTime refundInitiatedAt;

    private String upgradeStatus;
    private String upgradedFrom;
    private String upgradedTo;

    private String rescheduled;
    private Integer rescheduledCount;
    private LocalDate originalBookingDate;
    private String originalTimeSlot;
    private LocalDateTime rescheduledAt;
    private String rescheduledReason;

    public static BookingResponseDto fromEntity(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .bookingDate(booking.getBookingDate())
                .timeSlot(booking.getTimeSlot())
                .carType(booking.getCarType())
                .serviceType(booking.getServiceType())
                .washType(booking.getWashType())
                .address(booking.getAddress())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .createdAt(booking.getCreatedAt())
                .carNumber(booking.getCarNumber())
                .carIndex(booking.getCarIndex())
                .serviceCentreId(booking.getServiceCentreId())
                .centreName(booking.getCentreName())
                .mapsUrl(booking.getMapsUrl())
                .originalAmount(booking.getOriginalAmount())
                .payableAmount(booking.getPayableAmount())
                .discountPercentApplied(booking.getDiscountPercentApplied())
                .freeApplied(booking.getFreeApplied())
                .subscriptionRedeemed(booking.getSubscriptionRedeemed())
                .waterProvided(booking.getWaterProvided())
                .waterDiscountApplied(booking.getWaterDiscountApplied())
                .refundStatus(booking.getRefundStatus())
                .refundAmount(booking.getRefundAmount())
                .refundInitiatedAt(booking.getRefundInitiatedAt())
                .upgradeStatus(booking.getUpgradeStatus())
                .upgradedFrom(booking.getUpgradedFrom())
                .upgradedTo(booking.getUpgradedTo())
                .rescheduled(booking.getRescheduled())
                .rescheduledCount(booking.getRescheduledCount())
                .originalBookingDate(booking.getOriginalBookingDate())
                .originalTimeSlot(booking.getOriginalTimeSlot())
                .rescheduledAt(booking.getRescheduledAt())
                .rescheduledReason(booking.getRescheduledReason())
                .build();
    }
}