package com.carwash.rates.service;

import com.carwash.rates.common.VehicleType;
import com.carwash.rates.common.WashLevel;
import com.carwash.rates.dto.CentreRateResponse;
import com.carwash.rates.entity.CentreRate;
import com.carwash.rates.entity.Rate;
import com.carwash.rates.repository.CentreRateRepository;
import com.carwash.rates.repository.RateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves the effective price for a (centre, vehicle, wash) combination.
 * Order: per-centre override (active, time-bounded) -> global default rate.
 */
@Service
public class CentreRateResolver {

  private final CentreRateRepository overrideRepo;
  private final RateRepository defaultRepo;

  public CentreRateResolver(CentreRateRepository overrideRepo, RateRepository defaultRepo) {
    this.overrideRepo = overrideRepo;
    this.defaultRepo = defaultRepo;
  }

  /**
   * Single price lookup with fallback to default.
   * If centreId is null, only the default catalog is consulted.
   */
  public CentreRateResponse resolve(Long centreId, VehicleType vehicleType, WashLevel washLevel) {
    if (centreId != null && centreId > 0) {
      Optional<CentreRate> override =
          overrideRepo.findFirstEffective(centreId, vehicleType, washLevel, LocalDateTime.now());
      if (override.isPresent()) {
        CentreRate o = override.get();
        return new CentreRateResponse(
            centreId,
            vehicleType.name(),
            washLevel.name(),
            o.getAmount(),
            o.getCurrency(),
            "OVERRIDE"
        );
      }
    }

    Optional<Rate> def = defaultRepo.findTopByVehicleTypeAndWashLevelAndActiveTrue(vehicleType, washLevel);
    if (def.isEmpty()) return null;
    Rate d = def.get();
    return new CentreRateResponse(
        centreId,
        vehicleType.name(),
        washLevel.name(),
        d.getAmount(),
        d.getCurrency(),
        "DEFAULT"
    );
  }

  /**
   * Full effective price grid for a centre. Useful for the booking screen
   * so the UI can render any (vehicle, wash) combination without N round-trips.
   *
   * Returns a map keyed by "VEHICLE_TYPE|WASH_LEVEL" -> response.
   */
  public Map<String, CentreRateResponse> matrixFor(Long centreId) {
    LocalDateTime now = LocalDateTime.now();
    Map<String, CentreRateResponse> out = new LinkedHashMap<>();

    // 1. Seed with all defaults.
    for (Rate d : defaultRepo.findByActiveTrueOrderByVehicleTypeAscWashLevelAsc()) {
      String key = key(d.getVehicleType(), d.getWashLevel());
      out.put(key, new CentreRateResponse(
          centreId,
          d.getVehicleType().name(),
          d.getWashLevel().name(),
          d.getAmount(),
          d.getCurrency(),
          "DEFAULT"
      ));
    }

    // 2. Apply overrides for this centre (later effectiveFrom wins).
    if (centreId != null && centreId > 0) {
      List<CentreRate> overrides = overrideRepo.findEffectiveForCentre(centreId, now);
      // sort ascending so a later effectiveFrom overwrites an earlier one
      overrides.sort((a, b) -> a.getEffectiveFrom().compareTo(b.getEffectiveFrom()));
      for (CentreRate o : overrides) {
        out.put(key(o.getVehicleType(), o.getWashLevel()), new CentreRateResponse(
            centreId,
            o.getVehicleType().name(),
            o.getWashLevel().name(),
            o.getAmount(),
            o.getCurrency(),
            "OVERRIDE"
        ));
      }
    }
    return out;
  }

  /**
   * Convenience for backend services that just need the BigDecimal price.
   */
  public BigDecimal priceFor(Long centreId, VehicleType vehicleType, WashLevel washLevel) {
    CentreRateResponse r = resolve(centreId, vehicleType, washLevel);
    return r == null ? null : r.getAmount();
  }

  private static String key(VehicleType v, WashLevel w) {
    return v.name() + "|" + w.name();
  }
}
