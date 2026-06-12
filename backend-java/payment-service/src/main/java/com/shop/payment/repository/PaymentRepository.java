package com.shop.payment.repository;

import com.shop.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    Optional<Payment> findByOrderId(UUID orderId);
    Optional<Payment> findByPaymentKey(String paymentKey);
}
