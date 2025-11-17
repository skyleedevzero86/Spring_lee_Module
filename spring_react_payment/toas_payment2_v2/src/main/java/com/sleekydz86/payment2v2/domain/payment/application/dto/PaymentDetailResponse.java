package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailResponse {
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
    private CardInfo card;
    private String accountBankCode;
    private String accountBankName;
    private String accountNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiredTime;
}
