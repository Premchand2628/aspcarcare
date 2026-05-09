package com.carwash.rates.controller;

import com.carwash.rates.common.VehicleType;
import com.carwash.rates.common.WashLevel;
import com.carwash.rates.dto.CentreRateResponse;
import com.carwash.rates.dto.CentreRateUpsertRequest;
import com.carwash.rates.entity.CentreRate;
import com.carwash.rates.repository.CentreRateRepository;
import com.carwash.rates.service.CentreRateResolver;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/rates/centre")
public class CentreRateController {

  private final CentreRateResolver resolver;
  private final CentreRateRepository repository;

  public CentreRateController(CentreRateResolver resolver, CentreRateRepository repository) {
    this.resolver = resolver;
    this.repository = repository;
  }

  // -----------------------------------------------------------------
  // PUBLIC READ
  // -----------------------------------------------------------------

  /** Effective price grid for one centre (defaults + overrides merged). */
  @GetMapping("/{centreId}")
  public ResponseEntity<Map<String, CentreRateResponse>> getMatrix(@PathVariable Long centreId) {
    return ResponseEntity.ok(resolver.matrixFor(centreId));
  }

  /** Single effective price. */
  @GetMapping("/{centreId}/price")
  public ResponseEntity<CentreRateResponse> getPrice(@PathVariable Long centreId,
                                                     @RequestParam String vehicleType,
                                                     @RequestParam String washLevel) {
    CentreRateResponse r = resolver.resolve(
        centreId,
        VehicleType.from(vehicleType),
        WashLevel.from(washLevel)
    );
    return r == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(r);
  }

  // -----------------------------------------------------------------
  // ADMIN MANAGEMENT
  // -----------------------------------------------------------------

  /** All overrides currently stored for a centre (history included). */
  @GetMapping("/{centreId}/overrides")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<CentreRate>> listOverrides(@PathVariable Long centreId) {
    return ResponseEntity.ok(
        repository.findByCentreIdOrderByVehicleTypeAscWashLevelAscEffectiveFromDesc(centreId)
    );
  }

  /**
   * Upsert: if an active row exists for (centre, vehicle, wash, effectiveFrom)
   * we update it; otherwise we insert a new one.
   * effectiveFrom defaults to now.
   */
  @PostMapping("/overrides")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CentreRate> upsert(@Valid @RequestBody CentreRateUpsertRequest req) {
    LocalDateTime effFrom = Optional.ofNullable(req.getEffectiveFrom()).orElse(LocalDateTime.now());

    Optional<CentreRate> existing =
        repository.findFirstEffective(req.getCentreId(), req.getVehicleType(), req.getWashLevel(), effFrom);

    CentreRate entity = existing.orElseGet(CentreRate::new);
    entity.setCentreId(req.getCentreId());
    entity.setVehicleType(req.getVehicleType());
    entity.setWashLevel(req.getWashLevel());
    entity.setAmount(req.getAmount());
    entity.setCurrency(Optional.ofNullable(req.getCurrency()).orElse("INR"));
    entity.setActive(Optional.ofNullable(req.getActive()).orElse(true));
    entity.setEffectiveFrom(effFrom);
    entity.setEffectiveTo(req.getEffectiveTo());

    return ResponseEntity.ok(repository.save(entity));
  }

  /** Hard delete (centre falls back to default). */
  @DeleteMapping("/overrides/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (!repository.existsById(id)) return ResponseEntity.notFound().build();
    repository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  /** Soft disable (keeps history, falls back to default). */
  @PatchMapping("/overrides/{id}/disable")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CentreRate> disable(@PathVariable Long id) {
    return repository.findById(id)
        .map(r -> {
          r.setActive(false);
          r.setEffectiveTo(LocalDateTime.now());
          return ResponseEntity.ok(repository.save(r));
        })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
