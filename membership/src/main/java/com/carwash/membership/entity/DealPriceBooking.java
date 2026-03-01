package com.carwash.membership.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "carwash_deal_price_booking")
public class DealPriceBooking {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String phone;

  @Column(name = "car_type", nullable = false, length = 40)
  private String carType;

  @Column(name = "service_type", nullable = false, length = 40)
  private String serviceType;

  @Column(name = "payment_status", nullable = false, length = 20)
  private String paymentStatus;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "refund_amount", precision = 12, scale = 2)
  private BigDecimal refundAmount;

  @Column(name = "refund_initiated_at")
  private LocalDateTime refundInitiatedAt;

  @Column(name = "refund_status", length = 30)
  private String refundStatus;

  @Column(name = "transaction_id", nullable = false, unique = true, length = 80)
  private String transactionId;

  @Column(name = "discount_percent_applied", precision = 6, scale = 2)
  private BigDecimal discountPercentApplied;

  @Column(name = "original_amount", precision = 12, scale = 2)
  private BigDecimal originalAmount;

  @Column(name = "payable_amount", precision = 12, scale = 2)
  private BigDecimal payableAmount;

  @Column(name = "wash_type", nullable = false, length = 80)
  private String washType;

  @Column(name = "water_provided", nullable = false, length = 1)
  private String waterProvided;

  @Column(name = "plan_type_code", nullable = false, length = 40)
  private String planTypeCode;

  @PrePersist
  public void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }

    if (paymentStatus == null || paymentStatus.isBlank()) {
      paymentStatus = "SUCCESS";
    }

    if (refundAmount == null) {
      refundAmount = BigDecimal.ZERO;
    }

    if (refundStatus == null || refundStatus.isBlank()) {
      refundStatus = "NOT_INITIATED";
    }

    if (transactionId == null || transactionId.isBlank()) {
      String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
      transactionId = "DPB-" + suffix;
    }
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getCarType() {
    return carType;
  }

  public void setCarType(String carType) {
    this.carType = carType;
  }

  public String getServiceType() {
    return serviceType;
  }

  public void setServiceType(String serviceType) {
    this.serviceType = serviceType;
  }

  public String getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public BigDecimal getRefundAmount() {
    return refundAmount;
  }

  public void setRefundAmount(BigDecimal refundAmount) {
    this.refundAmount = refundAmount;
  }

  public LocalDateTime getRefundInitiatedAt() {
    return refundInitiatedAt;
  }

  public void setRefundInitiatedAt(LocalDateTime refundInitiatedAt) {
    this.refundInitiatedAt = refundInitiatedAt;
  }

  public String getRefundStatus() {
    return refundStatus;
  }

  public void setRefundStatus(String refundStatus) {
    this.refundStatus = refundStatus;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public BigDecimal getDiscountPercentApplied() {
    return discountPercentApplied;
  }

  public void setDiscountPercentApplied(BigDecimal discountPercentApplied) {
    this.discountPercentApplied = discountPercentApplied;
  }

  public BigDecimal getOriginalAmount() {
    return originalAmount;
  }

  public void setOriginalAmount(BigDecimal originalAmount) {
    this.originalAmount = originalAmount;
  }

  public BigDecimal getPayableAmount() {
    return payableAmount;
  }

  public void setPayableAmount(BigDecimal payableAmount) {
    this.payableAmount = payableAmount;
  }

  public String getWashType() {
    return washType;
  }

  public void setWashType(String washType) {
    this.washType = washType;
  }

  public String getWaterProvided() {
    return waterProvided;
  }

  public void setWaterProvided(String waterProvided) {
    this.waterProvided = waterProvided;
  }

  public String getPlanTypeCode() {
    return planTypeCode;
  }

  public void setPlanTypeCode(String planTypeCode) {
    this.planTypeCode = planTypeCode;
  }
}
