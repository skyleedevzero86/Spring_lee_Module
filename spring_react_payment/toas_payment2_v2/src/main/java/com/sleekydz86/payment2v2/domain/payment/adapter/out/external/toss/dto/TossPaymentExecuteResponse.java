package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TossPaymentExecuteResponse {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("approvalTime")
    private String approvalTime;

    @JsonProperty("stateMsg")
    private String stateMsg;

    @JsonProperty("amount")
    private Integer amount;

    @JsonProperty("discountedAmount")
    private Integer discountedAmount;

    @JsonProperty("paidAmount")
    private Integer paidAmount;

    @JsonProperty("payMethod")
    private String payMethod;

    @JsonProperty("orderNo")
    private String orderNo;

    @JsonProperty("payToken")
    private String payToken;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("cashReceiptMgtKey")
    private String cashReceiptMgtKey;

    @JsonProperty("cardCompanyName")
    private String cardCompanyName;

    @JsonProperty("cardCompanyCode")
    private Integer cardCompanyCode;

    @JsonProperty("cardAuthorizationNo")
    private String cardAuthorizationNo;

    @JsonProperty("spreadOut")
    private Integer spreadOut;

    @JsonProperty("noInterest")
    private Boolean noInterest;

    @JsonProperty("salesCheckLinkUrl")
    private String salesCheckLinkUrl;

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

    @JsonProperty("accountBankCode")
    private String accountBankCode;

    @JsonProperty("accountBankName")
    private String accountBankName;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("errorCode")
    private String errorCode;

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}

