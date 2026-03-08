package com.carwash.invitation.dto;

import java.math.BigDecimal;

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
public class GenerateCouponRequest {
	private String createdByPhone;
	private String discountType;     // PERCENT / FLAT
	private BigDecimal discountValue;
	private Integer maxUses;         // default 1
	private Integer validDays;       // e.g., 30
	private BigDecimal minOrderAmount;
	private BigDecimal maxDiscountAmount;
	
    
}
