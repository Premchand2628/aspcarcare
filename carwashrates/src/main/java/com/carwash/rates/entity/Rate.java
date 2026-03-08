package com.carwash.rates.entity;


import com.carwash.rates.common.VehicleType;
import com.carwash.rates.common.WashLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)  // ✅ IMPORTANT
@Entity
@Table(
  name = "carwash_rates",
  uniqueConstraints = @UniqueConstraint(columnNames = {"vehicle_type", "wash_level"})
)
public class Rate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "vehicle_type", nullable = false, length = 20)
  private VehicleType vehicleType;

  @Enumerated(EnumType.STRING)
  @Column(name = "wash_level", nullable = false, length = 20)
  private WashLevel washLevel;

  @Column(name = "amount", nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false, length = 10)
  private String currency = "INR";

  @Column(name = "active", nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  // getters/setters
//  public Long getId() { return id; }
//  public void setId(Long id) { this.id = id; }
//
//  public VehicleType getVehicleType() { return vehicleType; }
//  public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
//
//  public WashLevel getWashLevel() { return washLevel; }
//  public void setWashLevel(WashLevel washLevel) { this.washLevel = washLevel; }
//
//  public BigDecimal getAmount() { return amount; }
//  public void setAmount(BigDecimal amount) { this.amount = amount; }
//
//  public String getCurrency() { return currency; }
//  public void setCurrency(String currency) { this.currency = currency; }
//
//  public boolean isActive() { return active; }
//  public void setActive(boolean active) { this.active = active; }
//
//  public LocalDateTime getCreatedAt() { return createdAt; }
//  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//  public LocalDateTime getUpdatedAt() { return updatedAt; }
//  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
