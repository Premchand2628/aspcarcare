package com.carwash.invitation.repository;

import com.carwash.invitation.entity.ReferralCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7
import java.util.Optional;

public interface ReferralCouponRepository extends JpaRepository<ReferralCoupon, Long> {
    Optional<ReferralCoupon> findByCouponCode(String couponCode);
    boolean existsByCouponCode(String couponCode);
<<<<<<< HEAD
    List<ReferralCoupon> findByCreatedByPhone(String createdByPhone);
=======
>>>>>>> 5b20c96468ae6092789845c2e494b661303e36d7
}
