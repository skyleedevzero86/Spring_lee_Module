package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class IssueCashReceiptCommand {
    private BigDecimal amount;
    private String orderId;
    private String orderName;
    private String type; // 소득공제, 지출증빙
    private String customerIdentityNumber;
    private BigDecimal taxFreeAmount;
}

