package com.carwash.rates.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class RateUpsertRequest {

  @NotBlank
  private String vehicleType; // HATCHBACK...

  @NotBlank
  private String washLevel;   // BASIC/FOAM/PREMIUM

  @NotNull
  private BigDecimal amount;

  private String currency = "INR";
  private Boolean active = true;

  public String getVehicleType() { return vehicleType; }
  public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

  public String getWashLevel() { return washLevel; }
  public void setWashLevel(String washLevel) { this.washLevel = washLevel; }

  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }

  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }

  public Boolean getActive() { return active; }
  public void setActive(Boolean active) { this.active = active; }
}
