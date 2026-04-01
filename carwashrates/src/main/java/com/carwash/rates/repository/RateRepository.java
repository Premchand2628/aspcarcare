package com.carwash.rates.repository;

import com.carwash.rates.common.VehicleType;
import com.carwash.rates.common.WashLevel;
import com.carwash.rates.entity.Rate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, Long> {

    Optional<Rate> findTopByVehicleTypeAndWashLevelAndActiveTrue(
            VehicleType vehicleType,
            WashLevel washLevel
    );

    List<Rate> findByActiveTrueOrderByVehicleTypeAscWashLevelAsc();

    @Query(value = "SELECT DISTINCT vehicle_type FROM carwash_rates WHERE active = true ORDER BY vehicle_type", nativeQuery = true)
    List<String> findDistinctActiveVehicleTypes();

    @Query(value = "SELECT DISTINCT wash_level FROM carwash_rates WHERE active = true ORDER BY wash_level", nativeQuery = true)
    List<String> findDistinctActiveWashLevels();
}
