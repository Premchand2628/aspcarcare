package com.carwash.membership.repository;

import com.carwash.membership.entity.DealPriceBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealPriceBookingRepository extends JpaRepository<DealPriceBooking, Long> {

  List<DealPriceBooking> findAllByPhoneOrderByCreatedAtDesc(String phone);
}
