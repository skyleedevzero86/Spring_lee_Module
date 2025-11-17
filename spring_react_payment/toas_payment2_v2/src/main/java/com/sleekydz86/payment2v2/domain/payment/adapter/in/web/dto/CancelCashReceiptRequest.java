package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CancelCashReceiptRequest {
    private BigDecimal amount;
}

