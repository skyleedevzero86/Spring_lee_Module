package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentRequest {
    @JsonProperty("apiKey")
    private String apiKey;

    @JsonProperty("orderNo")
    private String orderNo;

    @JsonProperty("productDesc")
    private String productDesc;

    @JsonProperty("retUrl")
    private String retUrl;

    @JsonProperty("retCancelUrl")
    private String retCancelUrl;

    @JsonProperty("retAppScheme")
    private String retAppScheme;

    @JsonProperty("autoExecute")
    private String autoExecute;

    @JsonProperty("resultCallback")
    private String resultCallback;

    @JsonProperty("callbackVersion")
    private String callbackVersion;

    @JsonProperty("amount")
    private Integer amount;

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

    @JsonProperty("expiredTime")
    private String expiredTime;

    @JsonProperty("enablePayMethods")
    private String enablePayMethods;

    @JsonProperty("cashReceipt")
    private Boolean cashReceipt;

    @JsonProperty("cashReceiptTradeOption")
    private String cashReceiptTradeOption;

    @JsonProperty("cardOptions")
    private Object cardOptions;

    @JsonProperty("installment")
    private String installment;
}

