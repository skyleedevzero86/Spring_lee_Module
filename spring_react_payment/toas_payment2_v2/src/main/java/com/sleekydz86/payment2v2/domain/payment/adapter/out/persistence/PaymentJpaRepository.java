package com.sleekydz86.payment2v2.domain.payment.adapter.out.persistence;

import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long>, PaymentRepository {
    @Override
    Optional<Payment> findByOrderNo(String orderNo);
    
    @Override
    Optional<Payment> findByPayToken(String payToken);
    
    @Override
    boolean existsByOrderNo(String orderNo);
}

