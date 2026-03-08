package com.carwash.bookingservice.dto;

import java.math.BigDecimal;

<<<<<<< HEAD
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class BookingRequest {

  @NotBlank(message = "Phone is required")
  @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
  private String phone;

  @NotBlank(message = "Date is required")
  @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be YYYY-MM-DD")
  private String date;

  @NotBlank(message = "Timeslot is required")
  @Size(max = 50, message = "Timeslot is too long")
  private String timeslot;

  @NotBlank(message = "Car type is required")
  @Size(max = 30, message = "Car type is too long")
  private String carType;

  @NotBlank(message = "Car number is required")
  @Size(max = 20, message = "Car number is too long")
  @Pattern(regexp = "^[A-Za-z0-9 -]+$", message = "Car number contains invalid characters")
  private String carNumber;

  @PositiveOrZero(message = "Car index must be zero or positive")
  private Integer carIndex;

  // ✅ This stays HOME / SELFDRIVE / ASPCARE (matches booking table)
  @NotBlank(message = "Service type is required")
  @Size(max = 20, message = "Service type is too long")
  private String serviceType;

  // ✅ BASIC / FOAM / PREMIUM (drives membership benefits + rates)
  @NotBlank(message = "Wash type is required")
  @Size(max = 20, message = "Wash type is too long")
  private String washType;

  @Size(max = 300, message = "Address is too long")
  private String address;

  @Size(max = 500, message = "Maps URL is too long")
  private String mapsUrl;

  @PositiveOrZero(message = "Service centre id must be zero or positive")
  private Long serviceCentreId;

  @Size(max = 120, message = "Centre name is too long")
  private String centreName;

  @Size(max = 100, message = "Transaction id is too long")
  private String transactionId;

  // ✅ Optional: UI-calculated base amount (original before membership discount)
  @PositiveOrZero(message = "Base amount cannot be negative")
  private BigDecimal baseAmount;
  private Boolean waterProvided;

  @PositiveOrZero(message = "Water discount cannot be negative")
  private BigDecimal waterDiscountApplied;

  @Size(max = 64, message = "Plan type code is too long")
  private String planTypeCode;

  private Boolean subscriptionRedeemed;
=======
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
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7
  
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
<<<<<<< HEAD

public String getPlanTypeCode() {
  return planTypeCode;
}

public void setPlanTypeCode(String planTypeCode) {
  this.planTypeCode = planTypeCode;
}

public Boolean getSubscriptionRedeemed() {
  return subscriptionRedeemed;
}

public void setSubscriptionRedeemed(Boolean subscriptionRedeemed) {
  this.subscriptionRedeemed = subscriptionRedeemed;
}
=======
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7
  
  
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
