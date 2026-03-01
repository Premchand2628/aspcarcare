package com.carwash.rates.repository;

import com.carwash.rates.entity.DealPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DealPriceRepository extends JpaRepository<DealPrice, Long> {
    List<DealPrice> findByDealServiceType(String dealServiceType);
    List<DealPrice> findByDealWashType(String dealWashType);
    List<DealPrice> findByDealCarType(String dealCarType);
    List<DealPrice> findByDealServiceTypeAndDealWashType(String dealServiceType, String dealWashType);
    List<DealPrice> findByDealServiceTypeAndDealCarType(String dealServiceType, String dealCarType);
    List<DealPrice> findByDealServiceTypeAndDealWashTypeAndDealCarType(String dealServiceType, String dealWashType, String dealCarType);
    List<DealPrice> findAll();
}
