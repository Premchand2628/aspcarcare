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
@Table(name = "carwash_service_centre")
public class ServiceCentre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 120)
    private String area;          // locality like "Miyapur"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;        // e.g., 4.8

    @Column(name = "maps_url", columnDefinition = "TEXT")
    private String mapsUrl;       // Google Maps share link

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (active == null) active = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====

 
}
