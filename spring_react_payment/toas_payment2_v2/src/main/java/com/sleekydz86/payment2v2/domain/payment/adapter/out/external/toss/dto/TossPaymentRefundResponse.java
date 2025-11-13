package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TossPaymentRefundResponse {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("refundNo")
    private String refundNo;

    @JsonProperty("refundableAmount")
    private Integer refundableAmount;

    @JsonProperty("discountedAmount")
    private Integer discountedAmount;

    @JsonProperty("paidAmount")
    private Integer paidAmount;

    @JsonProperty("refundedAmount")
    private Integer refundedAmount;

    @JsonProperty("refundedDiscountAmount")
    private Integer refundedDiscountAmount;

    @JsonProperty("refundedPaidAmount")
    private Integer refundedPaidAmount;

    @JsonProperty("approvalTime")
    private String approvalTime;

    @JsonProperty("cashReceiptMgtKey")
    private String cashReceiptMgtKey;

    @JsonProperty("payToken")
    private String payToken;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("cardMethodType")
    private String cardMethodType;

    @JsonProperty("cardNumber")
    private String cardNumber;

    @JsonProperty("cardUserType")
    private String cardUserType;

    @JsonProperty("cardBinNumber")
    private String cardBinNumber;

    @JsonProperty("cardNum4Print")
    private String cardNum4Print;

    @JsonProperty("salesCheckLinkUrl")
    private String salesCheckLinkUrl;

    @JsonProperty("accountBankCode")
    private String accountBankCode;

    @JsonProperty("accountBankName")
    private String accountBankName;

    @JsonProperty("accountNumber")
    private String accountNumber;

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}

