package com.sleekydz86.toaspayment.domain.payment;

import com.sleekydz86.toaspayment.infrastructure.external.dto.TossPaymentResponse;

public interface PaymentGateway {
    TossPaymentResponse confirmPayment(String paymentKey, String orderId, Integer amount);
    TossPaymentResponse refundPayment(String paymentKey, String refundReason);
}



