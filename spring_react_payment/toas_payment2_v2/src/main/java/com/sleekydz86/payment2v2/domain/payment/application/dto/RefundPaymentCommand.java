package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RefundPaymentCommand {
    private Long paymentId;
    private String refundNo;
    private String reason;
    private BigDecimal amount;
    private BigDecimal amountTaxFree;
    private BigDecimal amountTaxable;
    private BigDecimal amountVat;
    private BigDecimal amountServiceFee;
    private Boolean idempotent;
}

