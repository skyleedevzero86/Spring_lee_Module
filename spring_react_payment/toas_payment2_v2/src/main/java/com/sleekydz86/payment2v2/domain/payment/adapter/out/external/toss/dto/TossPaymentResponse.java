package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TossPaymentResponse {
    @JsonProperty("code")
    private Integer code;

    @JsonProperty("checkoutPage")
    private String checkoutPage;

    @JsonProperty("payToken")
    private String payToken;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("errorCode")
    private String errorCode;

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}

