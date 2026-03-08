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
public class RedeemCouponRequest {
	private String couponCode;
	private String userPhone;
	private String bookingId;        // or orderId
	private String transactionId;
	private BigDecimal orderAmount;
	
    
}
