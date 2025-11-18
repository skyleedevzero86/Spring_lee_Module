package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreatePaymentRequest {

    @NotBlank(message = "주문번호는 필수입니다.")
    @Size(max = 50, message = "주문번호는 50자 이하여야 합니다.")
    @Pattern(regexp = "^[0-9a-zA-Z_\\-:.^@]+$", message = "주문번호는 숫자, 영문자, 특수문자 _-:.^@만 사용 가능합니다.")
    private String orderNo;

    @NotBlank(message = "상품 설명은 필수입니다.")
    @Size(max = 255, message = "상품 설명은 255자 이하여야 합니다.")
    private String productDesc;

    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
    @Max(value = 1000000000, message = "결제 금액은 10억 원을 초과할 수 없습니다.")
    private BigDecimal amount;

    @NotNull(message = "비과세 금액은 필수입니다.")
    @Min(value = 0, message = "비과세 금액은 0원 이상이어야 합니다.")
    private BigDecimal amountTaxFree;

    @Min(value = 0, message = "과세 금액은 0원 이상이어야 합니다.")
    private BigDecimal amountTaxable;

    @Min(value = 0, message = "부가세는 0원 이상이어야 합니다.")
    private BigDecimal amountVat;

    @Min(value = 0, message = "봉사료는 0원 이상이어야 합니다.")
    private BigDecimal amountServiceFee;

    @Min(value = 0, message = "일회용컵 보증금은 0원 이상이어야 합니다.")
    private BigDecimal disposableCupDeposit;

    @NotBlank(message = "결제 완료 URL은 필수입니다.")
    @Size(max = 255, message = "결제 완료 URL은 255자 이하여야 합니다.")
    private String retUrl;

    @NotBlank(message = "결제 취소 URL은 필수입니다.")
    @Size(max = 255, message = "결제 취소 URL은 255자 이하여야 합니다.")
    private String retCancelUrl;

    @Size(max = 255, message = "앱 스킴은 255자 이하여야 합니다.")
    private String retAppScheme;

    private Boolean autoExecute;

    @Size(max = 500, message = "결제 결과 콜백 URL은 500자 이하여야 합니다.")
    private String resultCallback;

    @Size(max = 2, message = "콜백 버전은 2자 이하여야 합니다.")
    private String callbackVersion;

    private LocalDateTime expiredTime;

    @Size(max = 100, message = "결제수단 구분은 100자 이하여야 합니다.")
    private String enablePayMethods;

    private Boolean cashReceipt;

    @Size(max = 10, message = "현금영수증 발급 타입은 10자 이하여야 합니다.")
    private String cashReceiptTradeOption;

    private Object cardOptions;

    @Size(max = 10, message = "할부 제한 타입은 10자 이하여야 합니다.")
    private String installment;
}
