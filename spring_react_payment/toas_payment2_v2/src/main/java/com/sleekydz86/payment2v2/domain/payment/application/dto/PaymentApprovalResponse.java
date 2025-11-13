package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

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
    private Integer amount;
    private Integer discountedAmount;
    private Integer paidAmount;
    private String payMethod;
    private String transactionId;
}

