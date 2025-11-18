package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IssueCashReceiptRequest {
    @NotNull(message = "금액은 필수입니다.")
    private BigDecimal amount;

    @NotBlank(message = "주문번호는 필수입니다.")
    @Size(min = 6, max = 64, message = "주문번호는 6자 이상 64자 이하여야 합니다.")
    private String orderId;

    @NotBlank(message = "구매상품명은 필수입니다.")
    @Size(max = 100, message = "구매상품명은 최대 100자까지 입력 가능합니다.")
    private String orderName;

    @NotBlank(message = "현금영수증 종류는 필수입니다.")
    private String type;

    @NotBlank(message = "소비자 인증수단은 필수입니다.")
    @Size(max = 30, message = "소비자 인증수단은 최대 30자까지 입력 가능합니다.")
    private String customerIdentityNumber;

    private BigDecimal taxFreeAmount;
}

