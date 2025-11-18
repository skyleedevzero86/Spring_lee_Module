package com.sleekydz86.toaspayment.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PurchaseInitRequest(
        @NotNull Long eventId,
        @NotNull @Positive Integer amount
) {
}
