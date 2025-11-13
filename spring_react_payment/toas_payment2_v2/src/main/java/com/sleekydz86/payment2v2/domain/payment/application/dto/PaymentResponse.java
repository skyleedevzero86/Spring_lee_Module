package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponse {
    private Long id;
    private String orderNo;
    private String payToken;
    private String checkoutPage;
    private String productDesc;
    private String status;
}

