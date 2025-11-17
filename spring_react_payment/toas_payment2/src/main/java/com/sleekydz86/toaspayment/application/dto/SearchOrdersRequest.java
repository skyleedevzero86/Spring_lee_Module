package com.sleekydz86.toaspayment.application.dto;

import java.time.LocalDateTime;

public record SearchOrdersRequest(
        String orderId,
        Long memberId,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}





