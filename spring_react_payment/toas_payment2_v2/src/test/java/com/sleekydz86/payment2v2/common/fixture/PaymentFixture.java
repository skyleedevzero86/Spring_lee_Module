package com.sleekydz86.payment2v2.common.fixture;

import com.sleekydz86.payment2v2.domain.payment.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentFixture {

    public static Payment.PaymentBuilder 기본_결제_생성() {
        return Payment.builder()
                .userId(1L)
                .orderNo("ORDER-001")
                .productDesc("테스트 상품")
                .amount(new BigDecimal("10000"))
                .amountTaxFree(new BigDecimal("0"))
                .amountTaxable(new BigDecimal("9091"))
                .amountVat(new BigDecimal("909"))
                .retUrl("https://example.com/return")
                .retCancelUrl("https://example.com/cancel")
                .expiredTime(LocalDateTime.now().plusHours(1));
    }

    public static Payment 완료된_결제() {
        Payment payment = 기본_결제_생성()
                .payToken("test-pay-token-123")
                .checkoutPage("https://toss.im/checkout")
                .build();
        payment.completePayment(
                "카드",
                BigDecimal.ZERO,
                new BigDecimal("10000"),
                "20240101120000",
                "TXN-123456"
        );
        return payment;
    }

    public static Payment 대기중인_결제() {
        return 기본_결제_생성()
                .payToken("test-pay-token-456")
                .checkoutPage("https://toss.im/checkout")
                .build();
    }

    public static Payment 취소된_결제() {
        Payment payment = 완료된_결제();
        payment.cancel();
        return payment;
    }
}

