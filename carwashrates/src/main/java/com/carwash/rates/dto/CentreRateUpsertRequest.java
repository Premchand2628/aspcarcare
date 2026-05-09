package com.carwash.rates.dto;

import com.carwash.rates.common.VehicleType;
import com.carwash.rates.common.WashLevel;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CentreRateUpsertRequest {

  @NotNull
  private Long centreId;

  @NotNull
  private VehicleType vehicleType;

  @NotNull
  private WashLevel washLevel;

  @NotNull
  @Positive
  private BigDecimal amount;

  private String currency;       // default INR
  private Boolean active;        // default true
  private LocalDateTime effectiveFrom;
  private LocalDateTime effectiveTo;

  public Long getCentreId() { return centreId; }
  public void setCentreId(Long centreId) { this.centreId = centreId; }

  public VehicleType getVehicleType() { return vehicleType; }
  public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

  public WashLevel getWashLevel() { return washLevel; }
  public void setWashLevel(WashLevel washLevel) { this.washLevel = washLevel; }

  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }

  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }

  public Boolean getActive() { return active; }
  public void setActive(Boolean active) { this.active = active; }

  public LocalDateTime getEffectiveFrom() { return effectiveFrom; }
  public void setEffectiveFrom(LocalDateTime effectiveFrom) { this.effectiveFrom = effectiveFrom; }

  public LocalDateTime getEffectiveTo() { return effectiveTo; }
  public void setEffectiveTo(LocalDateTime effectiveTo) { this.effectiveTo = effectiveTo; }
}
