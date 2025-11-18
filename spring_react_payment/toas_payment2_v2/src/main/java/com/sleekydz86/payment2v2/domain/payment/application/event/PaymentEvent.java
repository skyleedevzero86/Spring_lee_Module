package com.sleekydz86.payment2v2.domain.payment.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public abstract class PaymentEvent extends ApplicationEvent {
    private final LocalDateTime occurredAt;
    private final Long paymentId;
    private final String orderNo;

    protected PaymentEvent(Object source, Long paymentId, String orderNo) {
        super(source);
        this.occurredAt = LocalDateTime.now();
        this.paymentId = paymentId;
        this.orderNo = orderNo;
    }
}
