package com.carwash.rates.controller;

import com.carwash.rates.entity.CarwashService;
import com.carwash.rates.repository.CarwashServiceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/services")
public class CarwashServiceController {

    private final CarwashServiceRepository repository;

    public CarwashServiceController(CarwashServiceRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Cacheable(value = "carwashServices", sync = true)
    public ResponseEntity<List<CarwashService>> getActiveServices() {
        List<CarwashService> body = repository.findByActiveTrueOrderBySortOrderAsc();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .body(body);
    }
}
