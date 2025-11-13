package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionInfo {
    private String stepType;
    private String transactionId;
    private Integer transactionAmount;
    private Integer discountedAmount;
    private Integer paidAmount;
    private Integer pointAmount;
    private String regTs;
}

