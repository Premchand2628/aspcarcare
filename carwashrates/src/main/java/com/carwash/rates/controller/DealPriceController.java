package com.carwash.rates.controller;

import com.carwash.rates.dto.DealPriceDTO;
import com.carwash.rates.entity.DealPrice;
import com.carwash.rates.service.DealPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/deal-prices")
@CrossOrigin(origins = "*")
public class DealPriceController {
    
    @Autowired
    private DealPriceService dealPriceService;
    
    // Get all deal prices
    @GetMapping
    public ResponseEntity<List<DealPriceDTO>> getAllDealPrices() {
        List<DealPriceDTO> dealPrices = dealPriceService.getAllDealPrices();
        return ResponseEntity.ok(dealPrices);
    }
    
    // Get deal price by ID
    @GetMapping("/{id}")
    public ResponseEntity<DealPriceDTO> getDealPriceById(@PathVariable Long id) {
        DealPriceDTO dealPrice = dealPriceService.getDealPriceById(id);
        if (dealPrice != null) {
            return ResponseEntity.ok(dealPrice);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Get deal prices by service type
    @GetMapping("/by-service-type/{serviceType}")
    public ResponseEntity<List<DealPriceDTO>> getDealPricesByServiceType(@PathVariable String serviceType) {
        List<DealPriceDTO> dealPrices = dealPriceService.getDealPricesByServiceType(serviceType);
        return ResponseEntity.ok(dealPrices);
    }
    
    // Get deal prices by wash type
    @GetMapping("/by-wash-type/{washType}")
    public ResponseEntity<List<DealPriceDTO>> getDealPricesByWashType(@PathVariable String washType) {
        List<DealPriceDTO> dealPrices = dealPriceService.getDealPricesByWashType(washType);
        return ResponseEntity.ok(dealPrices);
    }

    // Get deal prices by car type
    @GetMapping("/by-car-type/{carType}")
    public ResponseEntity<List<DealPriceDTO>> getDealPricesByCarType(@PathVariable String carType) {
        List<DealPriceDTO> dealPrices = dealPriceService.getDealPricesByCarType(carType);
        return ResponseEntity.ok(dealPrices);
    }
    
    // Get deal prices by service type and wash type
    @GetMapping("/by-service-type/{serviceType}/wash-type/{washType}")
    public ResponseEntity<List<DealPriceDTO>> getDealPricesByServiceTypeAndWashType(
            @PathVariable String serviceType,
            @PathVariable String washType) {
        List<DealPriceDTO> dealPrices = dealPriceService.getDealPricesByServiceTypeAndWashType(serviceType, washType);
        return ResponseEntity.ok(dealPrices);
    }

    // Get deal prices by service type and car type
    @GetMapping("/by-service-type/{serviceType}/car-type/{carType}")
    public ResponseEntity<List<DealPriceDTO>> getDealPricesByServiceTypeAndCarType(
            @PathVariable String serviceType,
            @PathVariable String carType) {
        List<DealPriceDTO> dealPrices = dealPriceService.getDealPricesByServiceTypeAndCarType(serviceType, carType);
        return ResponseEntity.ok(dealPrices);
    }

    // Get deal prices by service type, wash type and car type
    @GetMapping("/by-service-type/{serviceType}/wash-type/{washType}/car-type/{carType}")
    public ResponseEntity<List<DealPriceDTO>> getDealPricesByServiceTypeAndWashTypeAndCarType(
            @PathVariable String serviceType,
            @PathVariable String washType,
            @PathVariable String carType) {
        List<DealPriceDTO> dealPrices = dealPriceService.getDealPricesByServiceTypeAndWashTypeAndCarType(serviceType, washType, carType);
        return ResponseEntity.ok(dealPrices);
    }
    
    // Create new deal price
    @PostMapping
    public ResponseEntity<DealPriceDTO> createDealPrice(@RequestBody DealPrice dealPrice) {
        DealPriceDTO created = dealPriceService.createDealPrice(dealPrice);
        return ResponseEntity.ok(created);
    }
    
    // Update deal price
    @PutMapping("/{id}")
    public ResponseEntity<DealPriceDTO> updateDealPrice(@PathVariable Long id, @RequestBody DealPrice dealPrice) {
        DealPriceDTO updated = dealPriceService.updateDealPrice(id, dealPrice);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Delete deal price
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDealPrice(@PathVariable Long id) {
        boolean deleted = dealPriceService.deleteDealPrice(id);
        if (deleted) {
            return ResponseEntity.ok("Deal price deleted successfully");
        }
        return ResponseEntity.notFound().build();
    }
}
