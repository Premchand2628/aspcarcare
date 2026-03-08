package com.carwash.bookingservice.dto;

public class DealPriceBookingRedeemRequest {

  private String phone;
  private String planTypeCode;
  private String carType;
  private String serviceType;
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
