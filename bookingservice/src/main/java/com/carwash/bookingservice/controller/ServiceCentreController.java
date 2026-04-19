package com.carwash.bookingservice.controller;

import com.carwash.bookingservice.repository.ServiceCentreRepository;
import com.carwash.bookingservice.entity.ServiceCentre;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/centres")
public class ServiceCentreController {

    private final ServiceCentreRepository repository;

    public ServiceCentreController(ServiceCentreRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/search")
    @Cacheable(value = "centres", key = "#area")
    public ResponseEntity<List<ServiceCentre>> searchCentres(@RequestParam("area") String area) {
        if (area == null || area.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String key = area.trim();
        List<ServiceCentre> list =
                repository.findByActiveTrueAndAreaIgnoreCaseContainingOrActiveTrueAndAddressIgnoreCaseContaining(
                        key, key
                );
        return ResponseEntity.ok(list);
    }

    // 🔹 NEW: get distinct active areas for dropdown
    @GetMapping("/areas")
    @Cacheable(value = "areas")
    public ResponseEntity<List<String>> getAreas() {
        List<String> areas = repository.findDistinctAreas();
        return ResponseEntity.ok(areas);
    }
}

