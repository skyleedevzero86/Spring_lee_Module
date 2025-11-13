package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentExecuteRequest {
    @JsonProperty("apiKey")
    @NotBlank(message = "API Key는 필수입니다.")
    @Size(max = 30, message = "API Key는 최대 30자까지 입력 가능합니다.")
    private String apiKey;

    @JsonProperty("payToken")
    @NotBlank(message = "결제 토큰은 필수입니다.")
    @Size(max = 30, message = "결제 토큰은 최대 30자까지 입력 가능합니다.")
    private String payToken;

    @JsonProperty("orderNo")
    @Size(max = 50, message = "주문번호는 최대 50자까지 입력 가능합니다.")
    private String orderNo;
}

