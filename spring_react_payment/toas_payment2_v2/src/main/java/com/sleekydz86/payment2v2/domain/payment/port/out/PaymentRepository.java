package com.sleekydz86.payment2v2.domain.payment.port.out;

import com.sleekydz86.payment2v2.domain.payment.model.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findByOrderNo(String orderNo);
    Optional<Payment> findByPayToken(String payToken);
    boolean existsByOrderNo(String orderNo);
    Payment save(Payment payment);
}

