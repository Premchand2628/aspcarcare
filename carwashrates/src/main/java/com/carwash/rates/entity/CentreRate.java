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
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(
  name = "carwash_centre_rate",
  indexes = {
    @Index(name = "idx_centre_rate_lookup", columnList = "centre_id, car_type, wash_type, active")
  }
)
public class CentreRate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "centre_id", nullable = false)
  private Long centreId;

  @Enumerated(EnumType.STRING)
  @Column(name = "car_type", nullable = false, length = 20)
  private VehicleType vehicleType;

  @Enumerated(EnumType.STRING)
  @Column(name = "wash_type", nullable = false, length = 20)
  private WashLevel washLevel;

  /** Distinguishes IN_STORE vs DOORSTEP etc. Defaults to IN_STORE for admin overrides. */
  @Column(name = "service_mode", nullable = false, length = 20)
  private String serviceMode = "IN_STORE";

  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false, length = 10)
  private String currency = "INR";

  @Column(name = "active", nullable = false)
  private boolean active = true;

  @Column(name = "effective_from", nullable = false)
  private LocalDateTime effectiveFrom;

  @Column(name = "effective_to")
  private LocalDateTime effectiveTo;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    if (this.effectiveFrom == null) this.effectiveFrom = now;
    if (this.serviceMode == null || this.serviceMode.isBlank()) this.serviceMode = "IN_STORE";
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
