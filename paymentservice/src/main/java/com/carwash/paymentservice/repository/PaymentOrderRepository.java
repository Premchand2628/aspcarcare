package com.carwash.paymentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carwash.paymentservice.entity.PaymentOrder;

import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {
    Optional<PaymentOrder> findByOrderId(String orderId);
}
