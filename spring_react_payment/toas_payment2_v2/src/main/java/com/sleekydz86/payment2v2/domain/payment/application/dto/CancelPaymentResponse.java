package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CancelPaymentResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal balanceAmount;
    private List<CancelInfo> cancels;

    @Getter
    @Builder
    public static class CancelInfo {
        private String transactionKey;
        private String cancelReason;
        private BigDecimal cancelAmount;
        private String canceledAt;
        private String cancelStatus;
    }
}

