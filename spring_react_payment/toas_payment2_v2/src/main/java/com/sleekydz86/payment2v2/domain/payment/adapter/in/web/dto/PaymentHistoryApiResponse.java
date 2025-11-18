package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentHistoryApiResponse {
    private Long id;
    private String orderNo;
    private String transactionId;
    private String productDesc;
    private BigDecimal amount;
    private String status;
    private String payMethod;
    private LocalDateTime createdAt;
    private LocalDateTime paidTs;
    private String userName;
    private String userEmail;
}
