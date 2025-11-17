package com.sleekydz86.payment2v2.domain.payment.port.out;

import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface PaymentRepository {
    Optional<Payment> findByOrderNo(String orderNo);

    Optional<Payment> findByPayToken(String payToken);

    boolean existsByOrderNo(String orderNo);

    Payment save(Payment payment);

    List<Payment> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Payment> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Payment> findAllByOrderByCreatedAtDesc();

    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<Payment> findByIdAndUserId(Long id, Long userId);

    Optional<Payment> findById(Long id);
}
