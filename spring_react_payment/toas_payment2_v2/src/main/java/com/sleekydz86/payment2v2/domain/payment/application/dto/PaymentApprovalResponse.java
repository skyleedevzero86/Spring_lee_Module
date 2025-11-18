package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentApprovalResponse {
    private Long id;
    private String orderNo;
    private String payToken;
    private String status;
    private String mode;
    private String approvalTime;
    private String stateMsg;
    private BigDecimal amount;
    private BigDecimal discountedAmount;
    private BigDecimal paidAmount;
    private String payMethod;
    private String transactionId;
}
