package com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CashReceiptListApiResponse {
    private Boolean hasNext;
    private Long lastCursor;
    private List<CashReceiptApiResponse> data;
}

