package com.carwash.bookingservice.controller;

import com.carwash.bookingservice.repository.CarwashQuotationRepository;
import com.carwash.bookingservice.entity.CarwashQuotation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quotations")
@CrossOrigin(origins = "*")
public class QuotationController {

    private final CarwashQuotationRepository quotationRepository;

    public QuotationController(CarwashQuotationRepository quotationRepository) {
        this.quotationRepository = quotationRepository;
    }

    // ---------- USER / DASHBOARD: READ ONLY ----------

    @GetMapping
    public ResponseEntity<List<CarwashQuotation>> getAllForDashboard() {
        List<CarwashQuotation> list = quotationRepository.findAllByOrderByVehicleTypeAsc();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/vehicle/{vehicleType}")
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
    @PutMapping("/admin/bulk")
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
