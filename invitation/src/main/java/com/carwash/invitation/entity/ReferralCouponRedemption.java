package com.carwash.invitation.entity;

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
@Table(name="carwash_referral_coupon_redemption",
       uniqueConstraints = @UniqueConstraint(name="uq_coupon_used_by_booking", columnNames = {"coupon_code","booking_id"}))
public class ReferralCouponRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="coupon_id", nullable = false)
    private Long couponId;

    @Column(name="coupon_code", nullable = false, length = 30)
    private String couponCode;

    @Column(name="used_by_phone", nullable = false, length = 20)
    private String usedByPhone;

    @Column(name="booking_id", length = 40)
    private String bookingId;

    @Column(name="transaction_id", length = 80)
    private String transactionId;

    @Column(name="discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(name="order_amount", nullable = false)
    private BigDecimal orderAmount;

    private LocalDateTime redeemedAt;

    @PrePersist
    void onCreate() {
        redeemedAt = LocalDateTime.now();
    }

//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}
//
//	public Long getCouponId() {
//		return couponId;
//	}
//
//	public void setCouponId(Long couponId) {
//		this.couponId = couponId;
//	}
//
//	public String getCouponCode() {
//		return couponCode;
//	}
//
//	public void setCouponCode(String couponCode) {
//		this.couponCode = couponCode;
//	}
//
//	public String getUsedByPhone() {
//		return usedByPhone;
//	}
//
//	public void setUsedByPhone(String usedByPhone) {
//		this.usedByPhone = usedByPhone;
//	}
//
//	public String getBookingId() {
//		return bookingId;
//	}
//
//	public void setBookingId(String bookingId) {
//		this.bookingId = bookingId;
//	}
//
//	public String getTransactionId() {
//		return transactionId;
//	}
//
//	public void setTransactionId(String transactionId) {
//		this.transactionId = transactionId;
//	}
//
//	public BigDecimal getDiscountAmount() {
//		return discountAmount;
//	}
//
//	public void setDiscountAmount(BigDecimal discountAmount) {
//		this.discountAmount = discountAmount;
//	}
//
//	public BigDecimal getOrderAmount() {
//		return orderAmount;
//	}
//
//	public void setOrderAmount(BigDecimal orderAmount) {
//		this.orderAmount = orderAmount;
//	}
//
//	public LocalDateTime getRedeemedAt() {
//		return redeemedAt;
//	}
//
//	public void setRedeemedAt(LocalDateTime redeemedAt) {
//		this.redeemedAt = redeemedAt;
//	}

 
}
