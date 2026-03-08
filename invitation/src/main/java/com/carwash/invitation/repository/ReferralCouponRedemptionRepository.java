package com.carwash.invitation.repository;

import com.carwash.invitation.entity.ReferralCouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReferralCouponRedemptionRepository extends JpaRepository<ReferralCouponRedemption, Long> {
    boolean existsByCouponCodeAndUsedByPhone(String couponCode, String usedByPhone);
    long countByCouponCode(String couponCode);
    List<ReferralCouponRedemption> findByCouponCodeIn(List<String> couponCodes);
}
