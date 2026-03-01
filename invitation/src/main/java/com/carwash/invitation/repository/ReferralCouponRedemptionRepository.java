package com.carwash.invitation.repository;

import com.carwash.invitation.entity.ReferralCouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralCouponRedemptionRepository extends JpaRepository<ReferralCouponRedemption, Long> {
    boolean existsByCouponCodeAndUsedByPhone(String couponCode, String usedByPhone);
    long countByCouponCode(String couponCode);
}
