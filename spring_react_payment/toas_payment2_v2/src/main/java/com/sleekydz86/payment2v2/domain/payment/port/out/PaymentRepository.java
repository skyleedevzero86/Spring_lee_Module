package com.sleekydz86.payment2v2.domain.payment.port.out;

import com.sleekydz86.payment2v2.domain.payment.model.Payment;

import java.util.Optional;

import java.util.List;

public interface PaymentRepository {
    Optional<Payment> findByOrderNo(String orderNo);
    Optional<Payment> findByPayToken(String payToken);
    boolean existsByOrderNo(String orderNo);
    Payment save(Payment payment);
    List<Payment> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<Payment> findAllByOrderByCreatedAtDesc();
    Optional<Payment> findByIdAndUserId(Long id, Long userId);
}

