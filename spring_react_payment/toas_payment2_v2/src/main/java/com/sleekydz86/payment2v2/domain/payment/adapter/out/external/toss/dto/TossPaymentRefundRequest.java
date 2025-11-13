package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentRefundRequest {
    @JsonProperty("apiKey")
    @NotBlank(message = "API Key는 필수입니다.")
    @Size(max = 30, message = "API Key는 최대 30자까지 입력 가능합니다.")
    private String apiKey;

    @JsonProperty("payToken")
    @NotBlank(message = "결제 토큰은 필수입니다.")
    @Size(max = 30, message = "결제 토큰은 최대 30자까지 입력 가능합니다.")
    private String payToken;

    @JsonProperty("refundNo")
    @NotBlank(message = "환불 번호는 필수입니다.")
    @Size(max = 36, message = "환불 번호는 최대 36자까지 입력 가능합니다.")
    private String refundNo;

    @JsonProperty("idempotent")
    private Boolean idempotent;

    @JsonProperty("reason")
    @Size(max = 255, message = "환불 사유는 최대 255자까지 입력 가능합니다.")
    private String reason;

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
}

