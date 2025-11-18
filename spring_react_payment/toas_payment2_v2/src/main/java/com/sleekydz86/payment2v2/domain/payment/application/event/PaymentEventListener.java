package com.sleekydz86.payment2v2.domain.payment.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    @Async("taskExecutor")
    @EventListener
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        log.info("결제 생성 이벤트 처리: paymentId={}, orderNo={}, userId={}",
                event.getPaymentId(), event.getOrderNo(), event.getUserId());
    }

    @Async("taskExecutor")
    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 처리: paymentId={}, orderNo={}, amount={}, transactionId={}",
                event.getPaymentId(), event.getOrderNo(), event.getAmount(), event.getTransactionId());
    }

    @Async("taskExecutor")
    @EventListener
    public void handlePaymentRefunded(PaymentRefundedEvent event) {
        log.info("결제 환불 이벤트 처리: paymentId={}, orderNo={}, refundNo={}, refundedAmount={}",
                event.getPaymentId(), event.getOrderNo(), event.getRefundNo(), event.getRefundedAmount());
    }
}
