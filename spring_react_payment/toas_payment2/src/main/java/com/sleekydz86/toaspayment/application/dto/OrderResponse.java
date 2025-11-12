package com.sleekydz86.toaspayment.application.dto;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        String orderId,
        String orderName,
        Long memberId,
        Integer amount,
        String paymentMethod,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

