package com.carwash.invitation.repository;

import com.carwash.invitation.entity.ReferralCouponRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.List;

public interface ReferralCouponRedemptionRepository extends JpaRepository<ReferralCouponRedemption, Long> {
    boolean existsByCouponCodeAndUsedByPhone(String couponCode, String usedByPhone);
    long countByCouponCode(String couponCode);
    List<ReferralCouponRedemption> findByCouponCodeIn(List<String> couponCodes);
=======
public interface ReferralCouponRedemptionRepository extends JpaRepository<ReferralCouponRedemption, Long> {
    boolean existsByCouponCodeAndUsedByPhone(String couponCode, String usedByPhone);
    long countByCouponCode(String couponCode);
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7
}
