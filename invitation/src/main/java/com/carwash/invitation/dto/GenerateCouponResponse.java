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
public class GenerateCouponResponse {
	private String couponCode;
	private String shareText;
	

}
