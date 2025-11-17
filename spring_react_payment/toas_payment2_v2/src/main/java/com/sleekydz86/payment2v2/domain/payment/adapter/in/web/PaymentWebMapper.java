package com.sleekydz86.payment2v2.domain.payment.adapter.in.web;

import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.ApprovePaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.CardInfoApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.CreatePaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.GetPaymentStatusRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentApprovalApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentDetailApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentHistoryApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PaymentStatusApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.RefundPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.RefundPaymentApiResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.CancelPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.CancelPaymentApiResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CancelPaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CancelPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.TransactionInfoApiResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.ApprovePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CreatePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.GetPaymentStatusCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentApprovalResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentDetailResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentHistoryResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentStatusResponse;
import com.sleekydz86.payment2v2.global.dto.PageResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.RefundPaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.RefundPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.adapter.in.web.dto.PageApiResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentWebMapper {

    public CreatePaymentCommand toCommand(CreatePaymentRequest request, Long userId) {
        CreatePaymentCommand.CreatePaymentCommandBuilder builder = CreatePaymentCommand.builder()
                .userId(userId)
                .orderNo(request.getOrderNo())
                .productDesc(request.getProductDesc())
                .amount(request.getAmount())
                .amountTaxFree(request.getAmountTaxFree())
                .retUrl(request.getRetUrl())
                .retCancelUrl(request.getRetCancelUrl());

        if (request.getAmountTaxable() != null) {
            builder.amountTaxable(request.getAmountTaxable());
        }
        if (request.getAmountVat() != null) {
            builder.amountVat(request.getAmountVat());
        }
        if (request.getAmountServiceFee() != null) {
            builder.amountServiceFee(request.getAmountServiceFee());
        }
        if (request.getDisposableCupDeposit() != null) {
            builder.disposableCupDeposit(request.getDisposableCupDeposit());
        }
        if (request.getRetAppScheme() != null) {
            builder.retAppScheme(request.getRetAppScheme());
        }
        if (request.getAutoExecute() != null) {
            builder.autoExecute(request.getAutoExecute());
        }
        if (request.getResultCallback() != null) {
            builder.resultCallback(request.getResultCallback());
        }
        if (request.getCallbackVersion() != null) {
            builder.callbackVersion(request.getCallbackVersion());
        }
        if (request.getExpiredTime() != null) {
            builder.expiredTime(request.getExpiredTime());
        } else {
            builder.expiredTime(LocalDateTime.now().plusMinutes(15));
        }
        if (request.getEnablePayMethods() != null) {
            builder.enablePayMethods(request.getEnablePayMethods());
        }
        if (request.getCashReceipt() != null) {
            builder.cashReceipt(request.getCashReceipt());
        }
        if (request.getCashReceiptTradeOption() != null) {
            builder.cashReceiptTradeOption(request.getCashReceiptTradeOption());
        }
        if (request.getCardOptions() != null) {
            builder.cardOptions(request.getCardOptions());
        }
        if (request.getInstallment() != null) {
            builder.installment(request.getInstallment());
        }

        return builder.build();
    }

    public PaymentApiResponse toApiResponse(PaymentResponse response) {
        return PaymentApiResponse.builder()
                .id(response.getId())
                .orderNo(response.getOrderNo())
                .payToken(response.getPayToken())
                .checkoutPage(response.getCheckoutPage())
                .productDesc(response.getProductDesc())
                .status(response.getStatus())
                .build();
    }

    public ApprovePaymentCommand toApproveCommand(ApprovePaymentRequest request) {
        return ApprovePaymentCommand.builder()
                .payToken(request.getPayToken())
                .orderNo(request.getOrderNo())
                .build();
    }

    public PaymentApprovalApiResponse toApprovalApiResponse(PaymentApprovalResponse response) {
        return PaymentApprovalApiResponse.builder()
                .id(response.getId())
                .orderNo(response.getOrderNo())
                .payToken(response.getPayToken())
                .status(response.getStatus())
                .mode(response.getMode())
                .approvalTime(response.getApprovalTime())
                .stateMsg(response.getStateMsg())
                .amount(response.getAmount())
                .discountedAmount(response.getDiscountedAmount())
                .paidAmount(response.getPaidAmount())
                .payMethod(response.getPayMethod())
                .transactionId(response.getTransactionId())
                .build();
    }

    public GetPaymentStatusCommand toStatusCommand(GetPaymentStatusRequest request) {
        return GetPaymentStatusCommand.builder()
                .payToken(request.getPayToken())
                .orderNo(request.getOrderNo())
                .build();
    }

    public PaymentStatusApiResponse toStatusApiResponse(PaymentStatusResponse response) {
        PaymentStatusApiResponse.PaymentStatusApiResponseBuilder builder = PaymentStatusApiResponse.builder()
                .payToken(response.getPayToken())
                .orderNo(response.getOrderNo())
                .payStatus(response.getPayStatus())
                .payMethod(response.getPayMethod())
                .mode(response.getMode())
                .amount(response.getAmount())
                .discountedAmount(response.getDiscountedAmount())
                .discountAmountV2(response.getDiscountAmountV2())
                .paidPointV2(response.getPaidPointV2())
                .paidAmount(response.getPaidAmount())
                .refundableAmount(response.getRefundableAmount())
                .amountTaxFree(response.getAmountTaxFree())
                .amountTaxable(response.getAmountTaxable())
                .amountVat(response.getAmountVat())
                .amountServiceFee(response.getAmountServiceFee())
                .disposableCupDeposit(response.getDisposableCupDeposit())
                .accountBankCode(response.getAccountBankCode())
                .accountBankName(response.getAccountBankName())
                .accountNumber(response.getAccountNumber())
                .createdTs(response.getCreatedTs())
                .paidTs(response.getPaidTs());

        if (response.getCard() != null) {
            builder.card(CardInfoApiResponse.builder()
                    .noInterest(response.getCard().getNoInterest())
                    .spreadOut(response.getCard().getSpreadOut())
                    .cardAuthorizationNo(response.getCard().getCardAuthorizationNo())
                    .cardMethodType(response.getCard().getCardMethodType())
                    .cardUserType(response.getCard().getCardUserType())
                    .cardNumber(response.getCard().getCardNumber())
                    .cardBinNumber(response.getCard().getCardBinNumber())
                    .cardNum4Print(response.getCard().getCardNum4Print())
                    .salesCheckLinkUrl(response.getCard().getSalesCheckLinkUrl())
                    .cardCompanyName(response.getCard().getCardCompanyName())
                    .cardCompanyCode(response.getCard().getCardCompanyCode())
                    .build());
        }

        if (response.getTransactions() != null && !response.getTransactions().isEmpty()) {
            builder.transactions(response.getTransactions().stream()
                    .map(tx -> TransactionInfoApiResponse.builder()
                            .stepType(tx.getStepType())
                            .transactionId(tx.getTransactionId())
                            .transactionAmount(tx.getTransactionAmount())
                            .discountedAmount(tx.getDiscountedAmount())
                            .paidAmount(tx.getPaidAmount())
                            .pointAmount(tx.getPointAmount())
                            .regTs(tx.getRegTs())
                            .build())
                    .toList());
        }

        return builder.build();
    }

    public PaymentHistoryApiResponse toHistoryApiResponse(PaymentHistoryResponse response) {
        return PaymentHistoryApiResponse.builder()
                .id(response.getId())
                .orderNo(response.getOrderNo())
                .transactionId(response.getTransactionId())
                .productDesc(response.getProductDesc())
                .amount(response.getAmount())
                .status(response.getStatus())
                .payMethod(response.getPayMethod())
                .createdAt(response.getCreatedAt())
                .paidTs(response.getPaidTs())
                .userName(response.getUserName())
                .userEmail(response.getUserEmail())
                .build();
    }

    public PaymentDetailApiResponse toDetailApiResponse(PaymentDetailResponse response) {
        PaymentDetailApiResponse.PaymentDetailApiResponseBuilder builder = PaymentDetailApiResponse.builder()
                .id(response.getId())
                .userId(response.getUserId())
                .orderNo(response.getOrderNo())
                .transactionId(response.getTransactionId())
                .productDesc(response.getProductDesc())
                .amount(response.getAmount())
                .amountTaxFree(response.getAmountTaxFree())
                .amountTaxable(response.getAmountTaxable())
                .amountVat(response.getAmountVat())
                .amountServiceFee(response.getAmountServiceFee())
                .disposableCupDeposit(response.getDisposableCupDeposit())
                .status(response.getStatus())
                .payMethod(response.getPayMethod())
                .discountedAmount(response.getDiscountedAmount())
                .paidAmount(response.getPaidAmount())
                .paidTs(response.getPaidTs())
                .mode(response.getMode())
                .approvalTime(response.getApprovalTime())
                .stateMsg(response.getStateMsg())
                .accountBankCode(response.getAccountBankCode())
                .accountBankName(response.getAccountBankName())
                .accountNumber(response.getAccountNumber())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .expiredTime(response.getExpiredTime());

        if (response.getCard() != null) {
            builder.card(CardInfoApiResponse.builder()
                    .noInterest(response.getCard().getNoInterest())
                    .spreadOut(response.getCard().getSpreadOut())
                    .cardAuthorizationNo(response.getCard().getCardAuthorizationNo())
                    .cardMethodType(response.getCard().getCardMethodType())
                    .cardUserType(response.getCard().getCardUserType())
                    .cardNumber(response.getCard().getCardNumber())
                    .cardBinNumber(response.getCard().getCardBinNumber())
                    .cardNum4Print(response.getCard().getCardNum4Print())
                    .salesCheckLinkUrl(response.getCard().getSalesCheckLinkUrl())
                    .cardCompanyName(response.getCard().getCardCompanyName())
                    .cardCompanyCode(response.getCard().getCardCompanyCode())
                    .build());
        }

        return builder.build();
    }

    public RefundPaymentCommand toRefundCommand(Long paymentId, RefundPaymentRequest request) {
        return RefundPaymentCommand.builder()
                .paymentId(paymentId)
                .refundNo(request.getRefundNo())
                .reason(request.getReason())
                .amount(request.getAmount())
                .amountTaxFree(request.getAmountTaxFree())
                .amountTaxable(request.getAmountTaxable())
                .amountVat(request.getAmountVat())
                .amountServiceFee(request.getAmountServiceFee())
                .idempotent(request.getIdempotent())
                .build();
    }

    public RefundPaymentApiResponse toRefundApiResponse(RefundPaymentResponse response) {
        return RefundPaymentApiResponse.builder()
                .paymentId(response.getPaymentId())
                .refundNo(response.getRefundNo())
                .refundableAmount(response.getRefundableAmount())
                .discountedAmount(response.getDiscountedAmount())
                .paidAmount(response.getPaidAmount())
                .refundedAmount(response.getRefundedAmount())
                .refundedDiscountAmount(response.getRefundedDiscountAmount())
                .refundedPaidAmount(response.getRefundedPaidAmount())
                .approvalTime(response.getApprovalTime())
                .cashReceiptMgtKey(response.getCashReceiptMgtKey())
                .payToken(response.getPayToken())
                .transactionId(response.getTransactionId())
                .cardMethodType(response.getCardMethodType())
                .cardNumber(response.getCardNumber())
                .cardUserType(response.getCardUserType())
                .cardBinNumber(response.getCardBinNumber())
                .cardNum4Print(response.getCardNum4Print())
                .salesCheckLinkUrl(response.getSalesCheckLinkUrl())
                .accountBankCode(response.getAccountBankCode())
                .accountBankName(response.getAccountBankName())
                .accountNumber(response.getAccountNumber())
                .status(response.getStatus())
                .build();
    }

    public <T, R> PageApiResponse<R> toPageApiResponse(PageResponse<T> pageResponse, java.util.function.Function<T, R> mapper) {
        List<R> content = pageResponse.getContent().stream()
                .map(mapper)
                .toList();

        return PageApiResponse.<R>builder()
                .content(content)
                .page(pageResponse.getPage())
                .size(pageResponse.getSize())
                .totalElements(pageResponse.getTotalElements())
                .totalPages(pageResponse.getTotalPages())
                .hasNext(pageResponse.isHasNext())
                .hasPrevious(pageResponse.isHasPrevious())
                .build();
    }

    public PageApiResponse<PaymentHistoryApiResponse> toPageApiResponse(PageResponse<PaymentHistoryResponse> pageResponse) {
        return toPageApiResponse(pageResponse, this::toHistoryApiResponse);
    }

    public CancelPaymentCommand toCancelCommand(String paymentKey, CancelPaymentRequest request) {
        CancelPaymentCommand.CancelPaymentCommandBuilder builder = CancelPaymentCommand.builder()
                .paymentKey(paymentKey)
                .cancelReason(request.getCancelReason())
                .cancelAmount(request.getCancelAmount())
                .taxFreeAmount(request.getTaxFreeAmount())
                .currency(request.getCurrency())
                .idempotencyKey(request.getIdempotencyKey());

        if (request.getRefundReceiveAccount() != null) {
            CancelPaymentCommand.RefundReceiveAccount refundAccount = 
                    CancelPaymentCommand.RefundReceiveAccount.builder()
                            .bank(request.getRefundReceiveAccount().getBank())
                            .accountNumber(request.getRefundReceiveAccount().getAccountNumber())
                            .holderName(request.getRefundReceiveAccount().getHolderName())
                            .build();
            builder.refundReceiveAccount(refundAccount);
        }

        return builder.build();
    }

    public CancelPaymentApiResponse toCancelApiResponse(CancelPaymentResponse response) {
        List<CancelPaymentApiResponse.CancelInfo> cancels = null;
        if (response.getCancels() != null) {
            cancels = response.getCancels().stream()
                    .map(cancel -> CancelPaymentApiResponse.CancelInfo.builder()
                            .transactionKey(cancel.getTransactionKey())
                            .cancelReason(cancel.getCancelReason())
                            .cancelAmount(cancel.getCancelAmount())
                            .canceledAt(cancel.getCanceledAt())
                            .cancelStatus(cancel.getCancelStatus())
                            .build())
                    .collect(java.util.stream.Collectors.toList());
        }

        return CancelPaymentApiResponse.builder()
                .paymentKey(response.getPaymentKey())
                .orderId(response.getOrderId())
                .orderName(response.getOrderName())
                .status(response.getStatus())
                .totalAmount(response.getTotalAmount())
                .balanceAmount(response.getBalanceAmount())
                .cancels(cancels)
                .build();
    }
}

