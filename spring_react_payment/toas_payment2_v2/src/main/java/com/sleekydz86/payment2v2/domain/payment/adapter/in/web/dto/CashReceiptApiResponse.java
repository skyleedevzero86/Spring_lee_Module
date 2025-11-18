package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CashReceiptApiResponse {
    private String receiptKey;
    private String orderId;
    private String orderName;
    private String type;
    private String issueNumber;
    private String receiptUrl;
    private String businessNumber;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal taxFreeAmount;
    private BigDecimal taxExemptionAmount;
    private String issueStatus;
    private String requestedAt;
    private String customerIdentityNumber;
    private FailureInfo failure;

    @Getter
    @Builder
    public static class FailureInfo {
        private String code;
        private String message;
    }
}
