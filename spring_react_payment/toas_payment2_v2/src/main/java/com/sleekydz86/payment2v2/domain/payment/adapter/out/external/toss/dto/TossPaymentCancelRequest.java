package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TossPaymentCancelRequest {
    @JsonProperty("cancelReason")
    @NotBlank(message = "취소 사유는 필수입니다.")
    @Size(max = 200, message = "취소 사유는 최대 200자까지 입력 가능합니다.")
    private String cancelReason;

    @JsonProperty("cancelAmount")
    private BigDecimal cancelAmount;

    @JsonProperty("taxFreeAmount")
    private BigDecimal taxFreeAmount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("refundReceiveAccount")
    private RefundReceiveAccount refundReceiveAccount;

    @Getter
    @Builder
    public static class RefundReceiveAccount {
        @JsonProperty("bank")
        @NotBlank(message = "은행 코드는 필수입니다.")
        private String bank;

        @JsonProperty("accountNumber")
        @NotBlank(message = "계좌번호는 필수입니다.")
        @Size(max = 20, message = "계좌번호는 최대 20자까지 입력 가능합니다.")
        private String accountNumber;

        @JsonProperty("holderName")
        @NotBlank(message = "예금주는 필수입니다.")
        @Size(max = 60, message = "예금주는 최대 60자까지 입력 가능합니다.")
        private String holderName;
    }
}
