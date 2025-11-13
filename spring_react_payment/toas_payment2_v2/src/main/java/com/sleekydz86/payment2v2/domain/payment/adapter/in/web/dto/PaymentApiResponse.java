package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentApiResponse {
    private Long id;
    private String orderNo;
    private String payToken;
    private String checkoutPage;
    private String productDesc;
    private String status;
}

