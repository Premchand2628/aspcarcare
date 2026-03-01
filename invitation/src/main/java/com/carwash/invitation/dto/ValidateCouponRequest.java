package com.carwash.invitation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.carwash.invitation.entity.ReferralCoupon;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)  // ✅ IMPORTANT
public class ValidateCouponRequest {
    private String couponCode;
    private String userPhone;          // who is trying to apply
    private BigDecimal orderAmount;
	
    
}
