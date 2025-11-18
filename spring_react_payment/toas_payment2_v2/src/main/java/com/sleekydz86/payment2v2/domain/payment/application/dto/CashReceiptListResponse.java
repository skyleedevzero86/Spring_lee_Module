package com.sleekydz86.payment2v2.domain.payment.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CashReceiptListResponse {
    private Boolean hasNext;
    private Long lastCursor;
    private List<CashReceiptResponse> data;
}

