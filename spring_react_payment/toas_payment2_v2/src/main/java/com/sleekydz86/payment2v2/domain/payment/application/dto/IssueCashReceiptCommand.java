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
    private String type;
    private String customerIdentityNumber;
    private BigDecimal taxFreeAmount;
}
