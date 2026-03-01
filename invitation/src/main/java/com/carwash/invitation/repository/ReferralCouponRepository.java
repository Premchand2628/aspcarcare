package com.carwash.invitation.repository;

import com.carwash.invitation.entity.ReferralCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReferralCouponRepository extends JpaRepository<ReferralCoupon, Long> {
    Optional<ReferralCoupon> findByCouponCode(String couponCode);
    boolean existsByCouponCode(String couponCode);
}
