package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentCallbackCommand {
    private String status;
    private String payToken;
    private String orderNo;
    private String payMethod;
    private Integer amount;
    private Integer discountedAmount;
    private Integer paidAmount;
    private String paidTs;
    private String transactionId;
    private Integer cardCompanyCode;
    private String cardAuthorizationNo;
    private String spreadOut;
    private Boolean noInterest;
    private String cardMethodType;
    private String cardUserType;
    private String cardNumber;
    private String cardBinNumber;
    private String cardNum4Print;
    private String salesCheckLinkUrl;
    private String accountBankCode;
    private String accountBankName;
    private String accountNumber;
}


