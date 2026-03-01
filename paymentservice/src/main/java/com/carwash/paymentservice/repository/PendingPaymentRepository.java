package com.carwash.paymentservice.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.carwash.paymentservice.entity.PendingPayment;

import java.util.Optional;

public interface PendingPaymentRepository extends JpaRepository<PendingPayment, Long> {
    Optional<PendingPayment> findByMerchantTransactionId(String merchantTransactionId);
}
