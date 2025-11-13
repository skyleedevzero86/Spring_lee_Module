package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundPaymentRequest {
    @NotBlank(message = "환불 번호는 필수입니다.")
    @Size(max = 36, message = "환불 번호는 최대 36자까지 입력 가능합니다.")
    private String refundNo;

    @Size(max = 255, message = "환불 사유는 최대 255자까지 입력 가능합니다.")
    private String reason;

    private BigDecimal amount;

    private BigDecimal amountTaxFree;

    private BigDecimal amountTaxable;

    private BigDecimal amountVat;

    private BigDecimal amountServiceFee;

    private Boolean idempotent;
}

