package com.carwash.bookingservice.controller;

import com.carwash.bookingservice.repository.CarwashQuotationRepository;
import com.carwash.bookingservice.entity.CarwashQuotation;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quotations")
public class QuotationController {

    private final CarwashQuotationRepository quotationRepository;

    public QuotationController(CarwashQuotationRepository quotationRepository) {
        this.quotationRepository = quotationRepository;
    }

    // ---------- USER / DASHBOARD: READ ONLY ----------

    @GetMapping
    @Cacheable(value = "quotations", key = "'all'")
    public ResponseEntity<List<CarwashQuotation>> getAllForDashboard() {
        List<CarwashQuotation> list = quotationRepository.findAllByOrderByVehicleTypeAsc();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/vehicle/{vehicleType}")
    @Cacheable(value = "quotations", key = "#vehicleType")
    public ResponseEntity<CarwashQuotation> getByVehicle(@PathVariable String vehicleType) {
        return quotationRepository.findByVehicleTypeIgnoreCase(vehicleType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------- ADMIN: EDIT & SAVE (BULK) ----------

    /**
     * Admin sends full list of rows (existing + new)
     * If id present => update; if null => create new row.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/bulk")
    @CacheEvict(value = "quotations", allEntries = true)
    public ResponseEntity<List<CarwashQuotation>> upsertAll(
            @RequestBody List<CarwashQuotation> payload
    ) {
        // simple upsert: vehicleType must be unique, but we rely on id when present
        for (CarwashQuotation q : payload) {
            if (q.getVehicleType() != null) {
                q.setVehicleType(q.getVehicleType().trim());
            }
        }
        List<CarwashQuotation> saved = quotationRepository.saveAll(payload);
        return ResponseEntity.ok(saved);
    }
}
