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
@Table(name = "carwash_referral_coupon")
public class ReferralCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="coupon_code", nullable = false, unique = true, length = 30)
    private String couponCode;

    @Column(name="created_by_phone", nullable = false, length = 20)
    private String createdByPhone;

    @Column(name="discount_type", nullable = false, length = 10)
    private String discountType; // PERCENT / FLAT

    @Column(name="discount_value", nullable = false)
    private BigDecimal discountValue;

    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;

    @Column(name="max_uses", nullable = false)
    private Integer maxUses = 1;

    @Column(name="used_count", nullable = false)
    private Integer usedCount = 0;

    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    @Column(name="status", nullable = false, length = 15)
    private String status = "ACTIVE";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (validFrom == null) validFrom = now;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
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
//	public String getCreatedByPhone() {
//		return createdByPhone;
//	}
//
//	public void setCreatedByPhone(String createdByPhone) {
//		this.createdByPhone = createdByPhone;
//	}
//
//	public String getDiscountType() {
//		return discountType;
//	}
//
//	public void setDiscountType(String discountType) {
//		this.discountType = discountType;
//	}
//
//	public BigDecimal getDiscountValue() {
//		return discountValue;
//	}
//
//	public void setDiscountValue(BigDecimal discountValue) {
//		this.discountValue = discountValue;
//	}
//
//	public BigDecimal getMinOrderAmount() {
//		return minOrderAmount;
//	}
//
//	public void setMinOrderAmount(BigDecimal minOrderAmount) {
//		this.minOrderAmount = minOrderAmount;
//	}
//
//	public BigDecimal getMaxDiscountAmount() {
//		return maxDiscountAmount;
//	}
//
//	public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
//		this.maxDiscountAmount = maxDiscountAmount;
//	}
//
//	public Integer getMaxUses() {
//		return maxUses;
//	}
//
//	public void setMaxUses(Integer maxUses) {
//		this.maxUses = maxUses;
//	}
//
//	public Integer getUsedCount() {
//		return usedCount;
//	}
//
//	public void setUsedCount(Integer usedCount) {
//		this.usedCount = usedCount;
//	}
//
//	public LocalDateTime getValidFrom() {
//		return validFrom;
//	}
//
//	public void setValidFrom(LocalDateTime validFrom) {
//		this.validFrom = validFrom;
//	}
//
//	public LocalDateTime getValidUntil() {
//		return validUntil;
//	}
//
//	public void setValidUntil(LocalDateTime validUntil) {
//		this.validUntil = validUntil;
//	}
//
//	public String getStatus() {
//		return status;
//	}
//
//	public void setStatus(String status) {
//		this.status = status;
//	}
//
//	public LocalDateTime getCreatedAt() {
//		return createdAt;
//	}
//
//	public void setCreatedAt(LocalDateTime createdAt) {
//		this.createdAt = createdAt;
//	}
//
//	public LocalDateTime getUpdatedAt() {
//		return updatedAt;
//	}
//
//	public void setUpdatedAt(LocalDateTime updatedAt) {
//		this.updatedAt = updatedAt;
//	}
//
//    
}
