package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.PurchaseInitRequest;
import com.sleekydz86.toaspayment.application.dto.PurchaseInitResponse;
import com.sleekydz86.toaspayment.domain.order.Order;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import com.sleekydz86.toaspayment.domain.order.valueobject.Money;
import com.sleekydz86.toaspayment.domain.order.valueobject.OrderId;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLog;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLogRepository;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLogType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitPurchaseUseCase {
    private final OrderRepository orderRepository;
    private final PaymentLogRepository paymentLogRepository;

    @Transactional
    public PurchaseInitResponse execute(PurchaseInitRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = Long.parseLong(authentication.getName());

        OrderId orderId = OrderId.generate();
        Money amount = Money.of(request.amount());
        Order order = Order.create(orderId, "예매 티켓", memberId, amount);

        orderRepository.save(order);

        PaymentLog paymentLog = PaymentLog.create(
                orderId.toString(),
                memberId,
                PaymentLogType.PAYMENT_INIT,
                "결제 초기화 - 금액: " + amount.toInteger() + "원"
        );
        paymentLogRepository.save(paymentLog);

        log.info("결제 초기화 완료 - 주문 ID: {}, 사용자 ID: {}, 금액: {}원", orderId, memberId, amount.toInteger());

        return new PurchaseInitResponse(
                new PurchaseInitResponse.PurchaseInitData(orderId.toString())
        );
    }
}

