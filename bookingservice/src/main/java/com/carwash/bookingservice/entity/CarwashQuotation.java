package com.carwash.bookingservice.entity;

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
@Table(name = "carwash_quotation")
public class CarwashQuotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_type", nullable = false, unique = true)
    private String vehicleType;

    private BigDecimal interior;
    private BigDecimal exterior;

    @Column(name = "interior_exterior")
    private BigDecimal interiorExterior;

    private BigDecimal teflon;

    @Column(name = "exterior_teflon")
    private BigDecimal exteriorTeflon;

    @Column(name = "interior_exterior_teflon")
    private BigDecimal interiorExteriorTeflon;

    @Column(name = "save_exterior_teflon")
    private BigDecimal saveExteriorTeflon;

    @Column(name = "save_full")
    private BigDecimal saveFull;

    // ---- getters & setters ----


}
