package com.carwash.rates.dto;

import java.math.BigDecimal;

/**
 * Effective price for a (centre, vehicle, wash) combination.
 * source = OVERRIDE if pulled from carwash_centre_rate, DEFAULT if from carwash_rates.
 */
public class CentreRateResponse {
  private Long centreId;
  private String vehicleType;
  private String washLevel;
  private BigDecimal amount;
  private String currency;
  private String source; // OVERRIDE | DEFAULT

  public CentreRateResponse() {}

  public CentreRateResponse(Long centreId, String vehicleType, String washLevel,
                            BigDecimal amount, String currency, String source) {
    this.centreId = centreId;
    this.vehicleType = vehicleType;
    this.washLevel = washLevel;
    this.amount = amount;
    this.currency = currency;
    this.source = source;
  }

  public Long getCentreId() { return centreId; }
  public void setCentreId(Long centreId) { this.centreId = centreId; }

  public String getVehicleType() { return vehicleType; }
  public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

  public String getWashLevel() { return washLevel; }
  public void setWashLevel(String washLevel) { this.washLevel = washLevel; }

  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }

  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }

  public String getSource() { return source; }
  public void setSource(String source) { this.source = source; }
}
