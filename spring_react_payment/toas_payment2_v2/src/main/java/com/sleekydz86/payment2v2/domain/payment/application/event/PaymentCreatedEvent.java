package com.sleekydz86.payment2v2.domain.payment.application.event;

import lombok.Getter;

@Getter
public class PaymentCreatedEvent extends PaymentEvent {
    private final Long userId;
    private final String productDesc;

    public PaymentCreatedEvent(Object source, Long paymentId, String orderNo, Long userId, String productDesc) {
        super(source, paymentId, orderNo);
        this.userId = userId;
        this.productDesc = productDesc;
    }
}
