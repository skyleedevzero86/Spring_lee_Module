package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TossCashReceiptResponse {
    @JsonProperty("receiptKey")
    private String receiptKey;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("orderName")
    private String orderName;

    @JsonProperty("type")
    private String type;

    @JsonProperty("issueNumber")
    private String issueNumber;

    @JsonProperty("receiptUrl")
    private String receiptUrl;

    @JsonProperty("businessNumber")
    private String businessNumber;

    @JsonProperty("transactionType")
    private String transactionType; // CONFIRM, CANCEL

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("taxFreeAmount")
    private BigDecimal taxFreeAmount;

    @JsonProperty("taxExemptionAmount")
    private BigDecimal taxExemptionAmount;

    @JsonProperty("issueStatus")
    private String issueStatus; // IN_PROGRESS, COMPLETED, FAILED

    @JsonProperty("failure")
    private FailureInfo failure;

    @JsonProperty("requestedAt")
    private String requestedAt;

    @JsonProperty("customerIdentityNumber")
    private String customerIdentityNumber;

    @Getter
    @Setter
    public static class FailureInfo {
        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;
    }
}

