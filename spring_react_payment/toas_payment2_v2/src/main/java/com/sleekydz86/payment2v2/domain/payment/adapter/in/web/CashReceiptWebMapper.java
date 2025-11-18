package com.sleekydz86.payment2v2.domain.payment.adapter.in.web;

import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.*;
import com.sleekydz86.payment2v2.domain.payment.application.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CashReceiptWebMapper {

    public IssueCashReceiptCommand toIssueCommand(IssueCashReceiptRequest request) {
        return IssueCashReceiptCommand.builder()
                .amount(request.getAmount())
                .orderId(request.getOrderId())
                .orderName(request.getOrderName())
                .type(request.getType())
                .customerIdentityNumber(request.getCustomerIdentityNumber())
                .taxFreeAmount(request.getTaxFreeAmount())
                .build();
    }

    public CancelCashReceiptCommand toCancelCommand(String receiptKey, CancelCashReceiptRequest request) {
        return CancelCashReceiptCommand.builder()
                .receiptKey(receiptKey)
                .amount(request.getAmount())
                .build();
    }

    public CashReceiptApiResponse toApiResponse(CashReceiptResponse response) {
        CashReceiptApiResponse.FailureInfo failureInfo = null;
        if (response.getFailure() != null) {
            failureInfo = CashReceiptApiResponse.FailureInfo.builder()
                    .code(response.getFailure().getCode())
                    .message(response.getFailure().getMessage())
                    .build();
        }

        return CashReceiptApiResponse.builder()
                .receiptKey(response.getReceiptKey())
                .orderId(response.getOrderId())
                .orderName(response.getOrderName())
                .type(response.getType())
                .issueNumber(response.getIssueNumber())
                .receiptUrl(response.getReceiptUrl())
                .businessNumber(response.getBusinessNumber())
                .transactionType(response.getTransactionType())
                .amount(response.getAmount())
                .taxFreeAmount(response.getTaxFreeAmount())
                .taxExemptionAmount(response.getTaxExemptionAmount())
                .issueStatus(response.getIssueStatus())
                .requestedAt(response.getRequestedAt())
                .customerIdentityNumber(response.getCustomerIdentityNumber())
                .failure(failureInfo)
                .build();
    }

    public CashReceiptListApiResponse toListApiResponse(CashReceiptListResponse response) {
        List<CashReceiptApiResponse> data = null;
        if (response.getData() != null) {
            data = response.getData().stream()
                    .map(this::toApiResponse)
                    .collect(Collectors.toList());
        }

        return CashReceiptListApiResponse.builder()
                .hasNext(response.getHasNext())
                .lastCursor(response.getLastCursor())
                .data(data)
                .build();
    }
}
