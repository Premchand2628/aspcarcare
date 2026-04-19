package com.carwash.rates.controller;

import com.carwash.rates.entity.CarwashService;
import com.carwash.rates.repository.CarwashServiceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
public class CarwashServiceController {

    private final CarwashServiceRepository repository;

    public CarwashServiceController(CarwashServiceRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Cacheable(value = "carwashServices")
    public ResponseEntity<List<CarwashService>> getActiveServices() {
        return ResponseEntity.ok(repository.findByActiveTrueOrderBySortOrderAsc());
    }
}
