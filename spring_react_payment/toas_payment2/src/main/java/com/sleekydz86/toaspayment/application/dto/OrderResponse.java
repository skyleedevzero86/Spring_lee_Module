package com.sleekydz86.toaspayment.application.dto;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String orderId,
        String originalOrderId,
        String orderName,
        Long memberId,
        Integer amount,
        String paymentMethod,
        String paymentMethodDisplay,
        String paymentKey,
        String status,
        String statusDisplay,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

