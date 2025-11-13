package com.sleekydz86.payment2v2.domain.payment.adapter.out.persistence;

import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentJpaRepository extends JpaRepository<Payment, Long>, PaymentRepository {
    @Override
    @Query("SELECT p FROM Payment p WHERE p.orderNo.value = :orderNo")
    Optional<Payment> findByOrderNo(@Param("orderNo") String orderNo);
    
    @Override
    Optional<Payment> findByPayToken(String payToken);
    
    @Override
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p WHERE p.orderNo.value = :orderNo")
    boolean existsByOrderNo(@Param("orderNo") String orderNo);
    
    @Override
    List<Payment> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Override
    List<Payment> findAllByOrderByCreatedAtDesc();
    
    @Override
    Optional<Payment> findByIdAndUserId(Long id, Long userId);
}

