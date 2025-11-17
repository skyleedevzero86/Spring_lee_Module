package com.sleekydz86.toaspayment.domain.paymentlog;

import java.util.List;
import java.util.Optional;

public interface PaymentLogRepository {
    PaymentLog save(PaymentLog paymentLog);
    List<PaymentLog> findByOrderId(String orderId);
    List<PaymentLog> findByMemberId(Long memberId);
    List<PaymentLog> findAll();
    Optional<PaymentLog> findById(Long id);
}




