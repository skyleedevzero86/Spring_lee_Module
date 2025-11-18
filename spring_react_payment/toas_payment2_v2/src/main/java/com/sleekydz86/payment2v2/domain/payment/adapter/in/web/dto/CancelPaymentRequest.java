package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CancelPaymentRequest {
    @NotBlank(message = "취소 사유는 필수입니다.")
    @Size(max = 200, message = "취소 사유는 최대 200자까지 입력 가능합니다.")
    private String cancelReason;

    private BigDecimal cancelAmount;

    private BigDecimal taxFreeAmount;

    private String currency;

    private RefundReceiveAccount refundReceiveAccount;

    private String idempotencyKey;

    @Getter
    @Setter
    public static class RefundReceiveAccount {
        @NotBlank(message = "은행 코드는 필수입니다.")
        private String bank;

        @NotBlank(message = "계좌번호는 필수입니다.")
        @Size(max = 20, message = "계좌번호는 최대 20자까지 입력 가능합니다.")
        private String accountNumber;

        @NotBlank(message = "예금주는 필수입니다.")
        @Size(max = 60, message = "예금주는 최대 60자까지 입력 가능합니다.")
        private String holderName;
    }
}

