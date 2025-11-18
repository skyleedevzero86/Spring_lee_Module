package com.sleekydz86.toaspayment.application.dto;

public record PurchaseInitResponse(
        PurchaseInitData data
) {
    public record PurchaseInitData(
            String purchaseUUID
    ) {
    }
}
