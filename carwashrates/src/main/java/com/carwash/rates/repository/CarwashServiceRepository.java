package com.carwash.rates.repository;

import com.carwash.rates.entity.CarwashService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarwashServiceRepository extends JpaRepository<CarwashService, Long> {

    List<CarwashService> findByActiveTrueOrderBySortOrderAsc();
}
