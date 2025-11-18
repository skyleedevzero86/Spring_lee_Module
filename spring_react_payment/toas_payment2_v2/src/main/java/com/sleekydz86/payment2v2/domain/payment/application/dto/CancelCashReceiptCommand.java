package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CancelCashReceiptCommand {
    private String receiptKey;
    private BigDecimal amount;
}
