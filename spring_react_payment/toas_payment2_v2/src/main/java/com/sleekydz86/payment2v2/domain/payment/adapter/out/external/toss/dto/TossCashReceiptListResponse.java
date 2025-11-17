package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TossCashReceiptListResponse {
    @JsonProperty("hasNext")
    private Boolean hasNext;

    @JsonProperty("lastCursor")
    private Long lastCursor;

    @JsonProperty("data")
    private List<TossCashReceiptResponse> data;
}

