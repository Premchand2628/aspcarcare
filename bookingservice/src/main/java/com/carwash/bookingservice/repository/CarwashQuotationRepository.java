package com.carwash.bookingservice.repository;

import com.carwash.bookingservice.entity.CarwashQuotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarwashQuotationRepository extends JpaRepository<CarwashQuotation, Long> {

    Optional<CarwashQuotation> findByVehicleTypeIgnoreCase(String vehicleType);

    List<CarwashQuotation> findAllByOrderByVehicleTypeAsc();
}
