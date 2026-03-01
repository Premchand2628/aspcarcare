package com.carwash.bookingservice.dto;

import java.math.BigDecimal;

public class BookingRequest {

  private String phone;
  private String date;
  private String timeslot;

  private String carType;
  private String carNumber;
  private Integer carIndex;

  // ✅ This stays HOME / SELFDRIVE / ASPCARE (matches booking table)
  private String serviceType;

  // ✅ BASIC / FOAM / PREMIUM (drives membership benefits + rates)
  private String washType;

  private String address;
  private String mapsUrl;

  private Long serviceCentreId;
  private String centreName;

  private String transactionId;

  // ✅ Optional: UI-calculated base amount (original before membership discount)
  private BigDecimal baseAmount;
  private Boolean waterProvided;
  private BigDecimal waterDiscountApplied;
  
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getDate() { return date; }
  public void setDate(String date) { this.date = date; }

  public String getTimeslot() { return timeslot; }
  public void setTimeslot(String timeslot) { this.timeslot = timeslot; }

  public String getCarType() { return carType; }
  public void setCarType(String carType) { this.carType = carType; }

  public String getCarNumber() { return carNumber; }
  public void setCarNumber(String carNumber) { this.carNumber = carNumber; }

  public Integer getCarIndex() { return carIndex; }
  public void setCarIndex(Integer carIndex) { this.carIndex = carIndex; }

  public String getServiceType() { return serviceType; }
  public void setServiceType(String serviceType) { this.serviceType = serviceType; }

  public String getWashType() { return washType; }
  public void setWashType(String washType) { this.washType = washType; }

  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }

  public String getMapsUrl() { return mapsUrl; }
  public void setMapsUrl(String mapsUrl) { this.mapsUrl = mapsUrl; }

  public Long getServiceCentreId() { return serviceCentreId; }
  public void setServiceCentreId(Long serviceCentreId) { this.serviceCentreId = serviceCentreId; }

  public String getCentreName() { return centreName; }
  public void setCentreName(String centreName) { this.centreName = centreName; }

  public String getTransactionId() { return transactionId; }
  public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

  public BigDecimal getBaseAmount() { return baseAmount; }
  public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
public Boolean getWaterProvided() {
	return waterProvided;
}
public void setWaterProvided(Boolean waterProvided) {
	this.waterProvided = waterProvided;
}
public BigDecimal getWaterDiscountApplied() {
	return waterDiscountApplied;
}
public void setWaterDiscountApplied(BigDecimal waterDiscountApplied) {
	this.waterDiscountApplied = waterDiscountApplied;
}
  
  
}

//package com.carwash.bookingservice.dto;
//
//import java.math.BigDecimal;
//
//public class BookingRequest {
//
//  private String phone;
//  private String date;
//  private String timeslot;
//  private String carType;
//  private String washType;
//  private String serviceType;
//  private String address;
//  private String carNumber;
//  private Integer carIndex;
//  private String mapsUrl;
//  private Long serviceCentreId;
//  private String centreName;
//  private String transactionId;
//
//  // IMPORTANT
//  private BigDecimal baseAmount;
//
//  public String getPhone() { return phone; }
//  public void setPhone(String phone) { this.phone = phone; }
//
//  public String getDate() { return date; }
//  public void setDate(String date) { this.date = date; }
//
//  public String getTimeslot() { return timeslot; }
//  public void setTimeslot(String timeslot) { this.timeslot = timeslot; }
//
//  public String getCarType() { return carType; }
//  public void setCarType(String carType) { this.carType = carType; }
//
//  public String getWashType() { return washType; }
//  public void setWashType(String washType) { this.washType = washType; }
//
//  public String getServiceType() { return serviceType; }
//  public void setServiceType(String serviceType) { this.serviceType = serviceType; }
//
//  public String getAddress() { return address; }
//  public void setAddress(String address) { this.address = address; }
//
//  public String getCarNumber() { return carNumber; }
//  public void setCarNumber(String carNumber) { this.carNumber = carNumber; }
//
//  public Integer getCarIndex() { return carIndex; }
//  public void setCarIndex(Integer carIndex) { this.carIndex = carIndex; }
//
//  public String getMapsUrl() { return mapsUrl; }
//  public void setMapsUrl(String mapsUrl) { this.mapsUrl = mapsUrl; }
//
//  public Long getServiceCentreId() { return serviceCentreId; }
//  public void setServiceCentreId(Long serviceCentreId) { this.serviceCentreId = serviceCentreId; }
//
//  public String getCentreName() { return centreName; }
//  public void setCentreName(String centreName) { this.centreName = centreName; }
//
//  public String getTransactionId() { return transactionId; }
//  public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
//
//  public BigDecimal getBaseAmount() { return baseAmount; }
//  public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
//}
//
