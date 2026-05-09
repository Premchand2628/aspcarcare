package com.carwash.bookingservice.controller;

import com.carwash.bookingservice.dto.ServiceCentreDto;
import com.carwash.bookingservice.repository.ServiceCentreRepository;
import com.carwash.bookingservice.service.CentreCodeGenerator;
import com.carwash.bookingservice.entity.ServiceCentre;

import jakarta.validation.Valid;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/centres")
public class ServiceCentreController {

    private final ServiceCentreRepository repository;
    private final CentreCodeGenerator codeGenerator;

    public ServiceCentreController(ServiceCentreRepository repository,
                                   CentreCodeGenerator codeGenerator) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
    }

    // -----------------------------------------------------------------
    // PUBLIC READ
    // -----------------------------------------------------------------

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

    // -----------------------------------------------------------------
    // ADMIN MANAGEMENT
    // -----------------------------------------------------------------

    /** Full list (active + inactive) for admin tables. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServiceCentreDto>> listAll() {
        return ResponseEntity.ok(
                repository.findAll().stream().map(ServiceCentreDto::fromEntity).toList()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceCentreDto> getOne(@PathVariable Long id) {
        return repository.findById(id)
                .map(ServiceCentreDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a centre. centreCode is server-generated from name+area+pincode.
     * The optional rate grid is NOT created here — AdminApp posts those
     * separately to /rates/centre/overrides so failures isolate cleanly.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "centres", allEntries = true)
    public ResponseEntity<ServiceCentreDto> create(@Valid @RequestBody ServiceCentreDto dto) {
        if (isBlank(dto.getName())) return ResponseEntity.badRequest().build();
        if (isBlank(dto.getAddress())) return ResponseEntity.badRequest().build();

        ServiceCentre entity = dto.toEntity();
        // server-controlled fields:
        entity.setCentreCode(codeGenerator.generate(dto.getName(), dto.getArea(), dto.getPincode()));
        if (entity.getActive() == null) entity.setActive(true);

        ServiceCentre saved = repository.save(entity);
        return ResponseEntity.ok(ServiceCentreDto.fromEntity(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "centres", allEntries = true)
    public ResponseEntity<ServiceCentreDto> update(@PathVariable Long id, @Valid @RequestBody ServiceCentreDto dto) {
        Optional<ServiceCentre> existing = repository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();

        ServiceCentre c = existing.get();
        c.setName(dto.getName());
        c.setArea(dto.getArea());
        c.setAddress(dto.getAddress());
        c.setPincode(dto.getPincode());
        c.setCity(dto.getCity());
        c.setState(dto.getState());
        c.setRating(dto.getRating());
        c.setBasePrice(dto.getBasePrice());
        c.setMapsUrl(dto.getMapsUrl());
        c.setLatitude(dto.getLat());
        c.setLongitude(dto.getLng());
        // centre_code stays stable across updates by design.
        return ResponseEntity.ok(ServiceCentreDto.fromEntity(repository.save(c)));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "centres", allEntries = true)
    public ResponseEntity<ServiceCentreDto> setActive(@PathVariable Long id, @RequestParam boolean active) {
        return repository.findById(id)
                .map(c -> { c.setActive(active); return repository.save(c); })
                .map(ServiceCentreDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "centres", allEntries = true)
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    // 🔹 NEW: get distinct active areas for dropdown
    @GetMapping("/areas")
    @Cacheable(value = "areas")
    public ResponseEntity<List<String>> getAreas() {
        List<String> areas = repository.findDistinctAreas();
        return ResponseEntity.ok(areas);
    }
}

