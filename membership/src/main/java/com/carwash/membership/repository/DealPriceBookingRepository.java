package com.carwash.membership.repository;

import com.carwash.membership.entity.DealPriceBooking;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
=======
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7

import java.util.List;

public interface DealPriceBookingRepository extends JpaRepository<DealPriceBooking, Long> {

  List<DealPriceBooking> findAllByPhoneOrderByCreatedAtDesc(String phone);
<<<<<<< HEAD

  @Query("""
      SELECT d
      FROM DealPriceBooking d
      WHERE d.phone = :phone
        AND UPPER(d.planTypeCode) = UPPER(:planTypeCode)
      ORDER BY d.createdAt DESC
      """)
  List<DealPriceBooking> findByPhoneAndPlanTypeCodeOrderByCreatedAtDesc(@Param("phone") String phone,
                                                                        @Param("planTypeCode") String planTypeCode);

  @Query("""
      SELECT d
      FROM DealPriceBooking d
      WHERE d.phone = :phone
        AND UPPER(d.planTypeCode) = UPPER(:planTypeCode)
        AND d.leftWashes > 0
      ORDER BY d.createdAt DESC
      """)
  List<DealPriceBooking> findRedeemableByPhoneAndPlanTypeCode(@Param("phone") String phone,
                                                               @Param("planTypeCode") String planTypeCode);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
      UPDATE carwash_deal_price_booking
      SET used_washes = COALESCE(used_washes, 0) + 1,
          left_washes = GREATEST(COALESCE(left_washes, 0) - 1, 0)
      WHERE id = :id
        AND COALESCE(left_washes, 0) > 0
      """, nativeQuery = true)
  int consumeWashIfAvailable(@Param("id") Long id);
=======
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7
}
