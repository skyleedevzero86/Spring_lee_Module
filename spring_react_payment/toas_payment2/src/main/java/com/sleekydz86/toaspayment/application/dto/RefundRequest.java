package com.sleekydz86.toaspayment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RefundRequest(
        @NotBlank String paymentKey,
        @NotBlank String orderId,
        @NotBlank String refundReason,
        @NotNull @Positive Integer paidAmount
) {
}

