package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CancelPaymentCommand {
    private String paymentKey;
    private String cancelReason;
    private BigDecimal cancelAmount;
    private BigDecimal taxFreeAmount;
    private String currency;
    private RefundReceiveAccount refundReceiveAccount;
    private String idempotencyKey; // 멱등키

    @Getter
    @Builder
    public static class RefundReceiveAccount {
        private String bank;
        private String accountNumber;
        private String holderName;
    }
}

