package com.carwash.membership.dto;

public class DealPriceBookingRevertRequest {

  private Long dealPriceBookingId;
  private String phone;
  private String planTypeCode;

  public Long getDealPriceBookingId() {
    return dealPriceBookingId;
  }

  public void setDealPriceBookingId(Long dealPriceBookingId) {
    this.dealPriceBookingId = dealPriceBookingId;
  }

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
}
