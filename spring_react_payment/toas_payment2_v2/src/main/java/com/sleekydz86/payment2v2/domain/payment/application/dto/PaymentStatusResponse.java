package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PaymentStatusResponse {
    private String payToken;
    private String orderNo;
    private String payStatus;
    private String payMethod;
    private String mode;
    private Integer amount;
    private Integer discountedAmount;
    private Integer discountAmountV2;
    private Integer paidPointV2;
    private Integer paidAmount;
    private Integer refundableAmount;
    private Integer amountTaxFree;
    private Integer amountTaxable;
    private Integer amountVat;
    private Integer amountServiceFee;
    private Integer disposableCupDeposit;
    private String accountBankCode;
    private String accountBankName;
    private String accountNumber;
    private CardInfo card;
    private List<TransactionInfo> transactions;
    private String createdTs;
    private String paidTs;
}

