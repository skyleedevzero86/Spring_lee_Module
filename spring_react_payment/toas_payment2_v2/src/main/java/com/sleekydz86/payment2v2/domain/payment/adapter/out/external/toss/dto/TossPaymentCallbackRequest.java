package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TossPaymentCallbackRequest {
    @JsonProperty("status")
    @NotBlank(message = "결제 상태는 필수입니다.")
    @Size(max = 20, message = "결제 상태는 최대 20자까지 입력 가능합니다.")
    private String status;

    @JsonProperty("payToken")
    @NotBlank(message = "결제 토큰은 필수입니다.")
    @Size(max = 30, message = "결제 토큰은 최대 30자까지 입력 가능합니다.")
    private String payToken;

    @JsonProperty("orderNo")
    @NotBlank(message = "주문번호는 필수입니다.")
    @Size(max = 50, message = "주문번호는 최대 50자까지 입력 가능합니다.")
    private String orderNo;

    @JsonProperty("payMethod")
    @Size(max = 10, message = "결제수단은 최대 10자까지 입력 가능합니다.")
    private String payMethod;

    @JsonProperty("amount")
    @NotNull(message = "결제요청 금액은 필수입니다.")
    private Integer amount;

    @JsonProperty("discountedAmount")
    private Integer discountedAmount;

    @JsonProperty("paidAmount")
    @NotNull(message = "지불수단 승인금액은 필수입니다.")
    private Integer paidAmount;

    @JsonProperty("paidTs")
    @Size(max = 20, message = "결제 완료 처리 시간은 최대 20자까지 입력 가능합니다.")
    private String paidTs;

    @JsonProperty("transactionId")
    @Size(max = 36, message = "거래 트랜잭션 아이디는 최대 36자까지 입력 가능합니다.")
    private String transactionId;

    @JsonProperty("cardCompanyCode")
    private Integer cardCompanyCode;

    @JsonProperty("cardAuthorizationNo")
    private String cardAuthorizationNo;

    @JsonProperty("spreadOut")
    private String spreadOut;

    @JsonProperty("noInterest")
    private Boolean noInterest;

    @JsonProperty("cardMethodType")
    private String cardMethodType;

    @JsonProperty("cardUserType")
    private String cardUserType;

    @JsonProperty("cardNumber")
    private String cardNumber;

    @JsonProperty("cardBinNumber")
    private String cardBinNumber;

    @JsonProperty("cardNum4Print")
    private String cardNum4Print;

    @JsonProperty("salesCheckLinkUrl")
    private String salesCheckLinkUrl;

    @JsonProperty("accountBankCode")
    private String accountBankCode;

    @JsonProperty("accountBankName")
    private String accountBankName;

    @JsonProperty("accountNumber")
    private String accountNumber;
}

