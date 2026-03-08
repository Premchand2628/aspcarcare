package com.carwash.bookingservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)  // ✅ IMPORTANT
@Entity
@Table(name = "carwash_refund")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    private String phone;
    private String carNumber;
    private String carType;
    private String serviceType;
    private String address;

    private LocalDate bookingDate;
    private String timeSlot;
    private String carIndex;

    private BigDecimal bookingAmount;
    private BigDecimal refundPercent;
    private BigDecimal refundAmount;
    private Double hoursRemaining;

    private String refundStatus;          // e.g. "INITIATED"
    private LocalDateTime initiatedAt;
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

	
    
}
