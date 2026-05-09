package com.carwash.rates.repository;

import com.carwash.rates.common.VehicleType;
import com.carwash.rates.common.WashLevel;
import com.carwash.rates.entity.CentreRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CentreRateRepository extends JpaRepository<CentreRate, Long> {

    @Query("""
        SELECT r FROM CentreRate r
         WHERE r.centreId = :centreId
           AND r.vehicleType = :vehicleType
           AND r.washLevel = :washLevel
           AND r.active = true
           AND r.effectiveFrom <= :now
           AND (r.effectiveTo IS NULL OR r.effectiveTo > :now)
         ORDER BY r.effectiveFrom DESC
        """)
    List<CentreRate> findEffective(@Param("centreId") Long centreId,
                                   @Param("vehicleType") VehicleType vehicleType,
                                   @Param("washLevel") WashLevel washLevel,
                                   @Param("now") LocalDateTime now);

    default Optional<CentreRate> findFirstEffective(Long centreId,
                                                    VehicleType vehicleType,
                                                    WashLevel washLevel,
                                                    LocalDateTime now) {
        List<CentreRate> hits = findEffective(centreId, vehicleType, washLevel, now);
        return hits.isEmpty() ? Optional.empty() : Optional.of(hits.get(0));
    }

    @Query("""
        SELECT r FROM CentreRate r
         WHERE r.centreId = :centreId
           AND r.active = true
           AND r.effectiveFrom <= :now
           AND (r.effectiveTo IS NULL OR r.effectiveTo > :now)
        """)
    List<CentreRate> findEffectiveForCentre(@Param("centreId") Long centreId,
                                            @Param("now") LocalDateTime now);

    List<CentreRate> findByCentreIdOrderByVehicleTypeAscWashLevelAscEffectiveFromDesc(Long centreId);
}
