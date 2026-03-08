package com.carwash.mailnotification.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailRequest {
    private String toEmail;
    private String firstName;
    private Long bookingId; 
    private String washType;
    private String carNumber;
    private String carType;
    private String bookingDate;
    private String timeSlot;
    private Double amount;
    private String action; // BOOKED, RESCHEDULED, CANCELLED, UPGRADED
    private String upgradedWashType; // For upgrade action
    private String originalWashType; // For upgrade action
}