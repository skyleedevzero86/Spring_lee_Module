package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TossPaymentStatusResponse {

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("payToken")
    private String payToken;

    @JsonProperty("payStatus")
    private String payStatus;

    @JsonProperty("orderNo")
    private String orderNo;

    @JsonProperty("payMethod")
    private String payMethod;

    @JsonProperty("amount")
    private Integer amount;

    @JsonProperty("discountedAmount")
    private Integer discountedAmount;

    @JsonProperty("discountAmountV2")
    private Integer discountAmountV2;

    @JsonProperty("paidPointV2")
    private Integer paidPointV2;

    @JsonProperty("paidAmount")
    private Integer paidAmount;

    @JsonProperty("refundableAmount")
    private Integer refundableAmount;

    @JsonProperty("amountTaxFree")
    private Integer amountTaxFree;

    @JsonProperty("amountTaxable")
    private Integer amountTaxable;

    @JsonProperty("amountVat")
    private Integer amountVat;

    @JsonProperty("amountServiceFee")
    private Integer amountServiceFee;

    @JsonProperty("disposableCupDeposit")
    private Integer disposableCupDeposit;

    @JsonProperty("accountBankCode")
    private String accountBankCode;

    @JsonProperty("accountBankName")
    private String accountBankName;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("card")
    private CardInfo card;

    @JsonProperty("transactions")
    private List<TransactionInfo> transactions;

    @JsonProperty("createdTs")
    private String createdTs;

    @JsonProperty("paidTs")
    private String paidTs;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("errorCode")
    private String errorCode;

    public boolean isSuccess() {
        return code != null && code == 0;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CardInfo {
        @JsonProperty("noInterest")
        private Boolean noInterest;

        @JsonProperty("spreadOut")
        private Integer spreadOut;

        @JsonProperty("cardAuthorizationNo")
        private String cardAuthorizationNo;

        @JsonProperty("cardMethodType")
        private String cardMethodType;

        @JsonProperty("cardUserType")
        private String cardUserType;

        @JsonProperty("cardNumber")
        private String cardNumber;

        @JsonProperty("cardBinNumber")
        private String cardBinNumber;

        @JsonProperty("cardNum4Print")
        private String cardNum4Print;

        @JsonProperty("salesCheckLinkUrl")
        private String salesCheckLinkUrl;

        @JsonProperty("cardCompanyName")
        private String cardCompanyName;

        @JsonProperty("cardCompanyCode")
        private Integer cardCompanyCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TransactionInfo {
        @JsonProperty("stepType")
        private String stepType;

        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("transactionAmount")
        private Integer transactionAmount;

        @JsonProperty("discountedAmount")
        private Integer discountedAmount;

        @JsonProperty("paidAmount")
        private Integer paidAmount;

        @JsonProperty("pointAmount")
        private Integer pointAmount;

        @JsonProperty("regTs")
        private String regTs;
    }
}

