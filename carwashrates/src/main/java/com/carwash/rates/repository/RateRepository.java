package com.carwash.rates.repository;

import com.carwash.rates.common.VehicleType;
import com.carwash.rates.common.WashLevel;
import com.carwash.rates.entity.Rate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, Long> {

    Optional<Rate> findTopByVehicleTypeAndWashLevelAndActiveTrue(
            VehicleType vehicleType,
            WashLevel washLevel
    );

    List<Rate> findByActiveTrueOrderByVehicleTypeAscWashLevelAsc();
}
