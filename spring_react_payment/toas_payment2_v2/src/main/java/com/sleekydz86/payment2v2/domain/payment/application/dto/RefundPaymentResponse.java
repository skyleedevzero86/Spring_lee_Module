package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefundPaymentResponse {
    private Long paymentId;
    private String refundNo;
    private Integer refundableAmount;
    private Integer discountedAmount;
    private Integer paidAmount;
    private Integer refundedAmount;
    private Integer refundedDiscountAmount;
    private Integer refundedPaidAmount;
    private String approvalTime;
    private String cashReceiptMgtKey;
    private String payToken;
    private String transactionId;
    private String cardMethodType;
    private String cardNumber;
    private String cardUserType;
    private String cardBinNumber;
    private String cardNum4Print;
    private String salesCheckLinkUrl;
    private String accountBankCode;
    private String accountBankName;
    private String accountNumber;
    private String status;
}
