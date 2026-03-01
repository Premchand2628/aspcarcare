package com.carwash.rates.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "carwash_deal_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealPrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "deal_service_type", nullable = false)
    private String dealServiceType; // Home, Self Drive
    
    @Column(name = "deal_wash_type", nullable = false)
    private String dealWashType; // Basic, Foam, Premium, Basic+Foam+Premium

    @Column(name = "deal_car_type", nullable = false)
    private String dealCarType; // Hatchback, Sedan, SUV, MPV, Pickup
    
    @Column(name = "deal_water_providing", nullable = false)
    private String dealWaterProviding; // Y, N
    
    @Column(name = "deal_actual_price", nullable = false)
    private BigDecimal dealActualPrice;
    
    @Column(name = "deal_discount", nullable = false)
    private BigDecimal dealDiscount; // percentage
    
    @Column(name = "deal_final_price", nullable = false)
    private BigDecimal dealFinalPrice;
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
