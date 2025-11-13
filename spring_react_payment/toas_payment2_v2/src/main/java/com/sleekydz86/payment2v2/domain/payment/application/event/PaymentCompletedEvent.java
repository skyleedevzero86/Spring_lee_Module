package com.sleekydz86.payment2v2.domain.payment.application.event;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentCompletedEvent extends PaymentEvent {
    private final BigDecimal amount;
    private final String transactionId;

    public PaymentCompletedEvent(Object source, Long paymentId, String orderNo, BigDecimal amount, String transactionId) {
        super(source, paymentId, orderNo);
        this.amount = amount;
        this.transactionId = transactionId;
    }
}

