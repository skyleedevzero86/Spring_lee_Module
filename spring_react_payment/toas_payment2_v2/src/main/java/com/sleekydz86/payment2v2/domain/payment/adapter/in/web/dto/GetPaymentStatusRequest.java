package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetPaymentStatusRequest {
    @Size(max = 50, message = "결제 토큰은 50자 이하여야 합니다.")
    private String payToken;

    @Size(max = 50, message = "주문번호는 50자 이하여야 합니다.")
    private String orderNo;

    @AssertTrue(message = "결제 토큰 또는 주문번호 중 하나는 필수입니다.")
    private boolean isValid() {
        return (payToken != null && !payToken.isBlank()) ||
               (orderNo != null && !orderNo.isBlank());
    }
}
