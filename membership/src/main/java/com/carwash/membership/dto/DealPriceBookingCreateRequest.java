package com.carwash.membership.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class DealPriceBookingCreateRequest {

  @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
  private String phone;

  @NotBlank(message = "Car type is required")
  @Size(max = 30, message = "Car type is too long")
  private String carType;

  @NotBlank(message = "Service type is required")
  @Size(max = 20, message = "Service type is too long")
  private String serviceType;

  @NotBlank(message = "Wash type is required")
  @Size(max = 20, message = "Wash type is too long")
  private String washType;

  @Pattern(regexp = "^(YES|NO|Y|N|yes|no|y|n)?$", message = "Water provided must be YES/NO or Y/N")
  private String waterProvided;

  @Size(max = 30, message = "Payment status is too long")
  private String paymentStatus;

  @PositiveOrZero(message = "Refund amount cannot be negative")
  private BigDecimal refundAmount;
  private LocalDateTime refundInitiatedAt;

  @Size(max = 30, message = "Refund status is too long")
  private String refundStatus;

  @Size(max = 100, message = "Transaction id is too long")
  private String transactionId;

  @PositiveOrZero(message = "Discount percent cannot be negative")
  private BigDecimal discountPercentApplied;

  @PositiveOrZero(message = "Original amount cannot be negative")
  private BigDecimal originalAmount;

  @PositiveOrZero(message = "Payable amount cannot be negative")
  private BigDecimal payableAmount;

  @Positive(message = "Total washes must be greater than zero")
  private Integer totalWashes;

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

  public String getPaymentStatus() {
    return paymentStatus;
  }

  public void setPaymentStatus(String paymentStatus) {
    this.paymentStatus = paymentStatus;
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

  public Integer getTotalWashes() {
    return totalWashes;
  }

  public void setTotalWashes(Integer totalWashes) {
    this.totalWashes = totalWashes;
  }
}
