package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CreatePaymentCommand {
    private Long userId;
    private String orderNo;
    private String productDesc;
    private BigDecimal amount;
    private BigDecimal amountTaxFree;
    private BigDecimal amountTaxable;
    private BigDecimal amountVat;
    private BigDecimal amountServiceFee;
    private BigDecimal disposableCupDeposit;
    private String retUrl;
    private String retCancelUrl;
    private String retAppScheme;
    private Boolean autoExecute;
    private String resultCallback;
    private String callbackVersion;
    private LocalDateTime expiredTime;
    private String enablePayMethods;
    private Boolean cashReceipt;
    private String cashReceiptTradeOption;
    private Object cardOptions;
    private String installment;
}
