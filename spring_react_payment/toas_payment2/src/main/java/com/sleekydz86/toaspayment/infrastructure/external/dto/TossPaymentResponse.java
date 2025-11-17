package com.sleekydz86.toaspayment.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TossPaymentResponse(
        String mId,
        String version,
        String paymentKey,
        String status,
        @JsonProperty("lastTransactionKey") String lastTransactionKey,
        String orderId,
        String orderName,
        String requestedAt,
        String approvedAt,
        Boolean useEscrow,
        Boolean cultureExpense,
        Card card,
        String type,
        String country,
        String currency,
        Integer totalAmount,
        Integer balanceAmount,
        Integer suppliedAmount,
        Integer vat,
        Integer taxFreeAmount,
        Integer taxExemptionAmount,
        String method,
        List<CancelDto> cancels
) {
    public record Card(
            String issuerCode,
            String acquirerCode,
            String number,
            Integer installmentPlanMonths,
            Boolean isInterestFree,
            String interestPayer,
            String approveNo,
            Boolean useCardPoint,
            String cardType,
            String ownerType,
            String acquireStatus,
            Integer amount
    ) {
    }

    public record CancelDto(
            String cancelReason,
            String canceledAt,
            Integer cancelAmount,
            Integer taxFreeAmount,
            Integer taxExemptionAmount,
            Integer refundableAmount,
            Integer transferDiscountAmount,
            Integer easyPayDiscountAmount,
            String transactionKey,
            String receiptKey,
            String cancelStatus,
            String cancelRequestId
    ) {
    }
}




