package com.carwash.membership.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DealPriceBookingRedeemRequest {

  @NotBlank(message = "phone is required")
  @Size(max = 20, message = "phone is too long")
  private String phone;

  @NotBlank(message = "planTypeCode is required")
  @Size(max = 40, message = "planTypeCode is too long")
  private String planTypeCode;

  @NotBlank(message = "carType is required")
  @Size(max = 40, message = "carType is too long")
  private String carType;

  @NotBlank(message = "serviceType is required")
  @Size(max = 40, message = "serviceType is too long")
  private String serviceType;

  @NotBlank(message = "washType is required")
  @Size(max = 80, message = "washType is too long")
  private String washType;

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getPlanTypeCode() {
    return planTypeCode;
  }

  public void setPlanTypeCode(String planTypeCode) {
    this.planTypeCode = planTypeCode;
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
}
