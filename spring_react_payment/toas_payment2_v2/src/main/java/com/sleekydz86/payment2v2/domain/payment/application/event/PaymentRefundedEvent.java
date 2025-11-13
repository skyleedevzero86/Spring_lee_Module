package com.sleekydz86.payment2v2.domain.payment.application.event;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentRefundedEvent extends PaymentEvent {
    private final String refundNo;
    private final BigDecimal refundedAmount;

    public PaymentRefundedEvent(Object source, Long paymentId, String orderNo, String refundNo, BigDecimal refundedAmount) {
        super(source, paymentId, orderNo);
        this.refundNo = refundNo;
        this.refundedAmount = refundedAmount;
    }
}

