package com.sleekydz86.toaspayment.application.dto;

import java.time.LocalDateTime;

public record PaymentLogResponse(
        Long id,
        String orderId,
        Long memberId,
        String logType,
        String message,
        String details,
        LocalDateTime createdAt
) {
}
