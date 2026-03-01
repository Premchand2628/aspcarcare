package com.carwash.membership.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DealPriceBookingCreateRequest {

  private String phone;
  private String carType;
  private String serviceType;
  private String washType;
  private String waterProvided;
  private String paymentStatus;
  private BigDecimal refundAmount;
  private LocalDateTime refundInitiatedAt;
  private String refundStatus;
  private String transactionId;
  private BigDecimal discountPercentApplied;
  private BigDecimal originalAmount;
  private BigDecimal payableAmount;

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
}
