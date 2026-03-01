package com.carwash.rates.dto;

import java.math.BigDecimal;

public class RateResponse {
  private String vehicleType;
  private String washLevel;
  private BigDecimal amount;
  private String currency;

  public RateResponse() {}

  public RateResponse(String vehicleType, String washLevel, BigDecimal amount, String currency) {
    this.vehicleType = vehicleType;
    this.washLevel = washLevel;
    this.amount = amount;
    this.currency = currency;
  }

  public String getVehicleType() { return vehicleType; }
  public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

  public String getWashLevel() { return washLevel; }
  public void setWashLevel(String washLevel) { this.washLevel = washLevel; }

  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }

  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }
}
