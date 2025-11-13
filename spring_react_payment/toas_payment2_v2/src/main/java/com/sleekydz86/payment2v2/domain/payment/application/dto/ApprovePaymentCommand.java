package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApprovePaymentCommand {
    private String payToken;
    private String orderNo;
}

