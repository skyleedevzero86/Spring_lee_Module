package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentDetailApiResponse {
    private Long id;
    private Long userId;
    private String orderNo;
    private String transactionId;
    private String productDesc;
    private BigDecimal amount;
    private BigDecimal amountTaxFree;
    private BigDecimal amountTaxable;
    private BigDecimal amountVat;
    private BigDecimal amountServiceFee;
    private BigDecimal disposableCupDeposit;
    private String status;
    private String payMethod;
    private BigDecimal discountedAmount;
    private BigDecimal paidAmount;
    private String paidTs;
    private String mode;
    private String approvalTime;
    private String stateMsg;
    private CardInfoApiResponse card;
    private String accountBankCode;
    private String accountBankName;
    private String accountNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiredTime;
}
