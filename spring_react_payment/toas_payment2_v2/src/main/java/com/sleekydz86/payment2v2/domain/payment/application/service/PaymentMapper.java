package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.payment.application.dto.CardInfo;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CreatePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentApprovalResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentDetailResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentHistoryResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentStatusResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.TransactionInfo;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentStatusResponse;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.global.config.TossPaymentProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class PaymentMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final RoundingMode AMOUNT_ROUNDING_MODE = RoundingMode.HALF_UP;

    private final String apiKey;

    public PaymentMapper(TossPaymentProperties tossPaymentProperties) {
        this.apiKey = tossPaymentProperties.getApi().getKey();
    }

    public TossPaymentRequest toTossRequest(CreatePaymentCommand command) {
        TossPaymentRequest.TossPaymentRequestBuilder builder = TossPaymentRequest.builder()
                .apiKey(apiKey)
                .orderNo(command.getOrderNo())
                .productDesc(command.getProductDesc())
                .retUrl(command.getRetUrl())
                .retCancelUrl(command.getRetCancelUrl())
                .amount(convertToInteger(command.getAmount()))
                .amountTaxFree(convertToInteger(command.getAmountTaxFree()));

        if (command.getAmountTaxable() != null) {
            builder.amountTaxable(convertToInteger(command.getAmountTaxable()));
        }
        if (command.getAmountVat() != null) {
            builder.amountVat(convertToInteger(command.getAmountVat()));
        }
        if (command.getAmountServiceFee() != null) {
            builder.amountServiceFee(convertToInteger(command.getAmountServiceFee()));
        }
        if (command.getDisposableCupDeposit() != null) {
            builder.disposableCupDeposit(convertToInteger(command.getDisposableCupDeposit()));
        }
        if (command.getRetAppScheme() != null) {
            builder.retAppScheme(command.getRetAppScheme());
        }
        if (command.getAutoExecute() != null) {
            builder.autoExecute(command.getAutoExecute().toString());
        }
        if (command.getResultCallback() != null) {
            builder.resultCallback(command.getResultCallback());
        }
        if (command.getCallbackVersion() != null) {
            builder.callbackVersion(command.getCallbackVersion());
        }
        if (command.getExpiredTime() != null) {
            builder.expiredTime(command.getExpiredTime().format(DATE_TIME_FORMATTER));
        }
        if (command.getEnablePayMethods() != null) {
            builder.enablePayMethods(command.getEnablePayMethods());
        }
        if (command.getCashReceipt() != null) {
            builder.cashReceipt(command.getCashReceipt());
        }
        if (command.getCashReceiptTradeOption() != null) {
            builder.cashReceiptTradeOption(command.getCashReceiptTradeOption());
        }
        if (command.getCardOptions() != null) {
            builder.cardOptions(command.getCardOptions());
        }
        if (command.getInstallment() != null) {
            builder.installment(command.getInstallment());
        }

        return builder.build();
    }

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderNo(payment.getOrderNoValue())
                .payToken(payment.getPayToken())
                .checkoutPage(payment.getCheckoutPage())
                .productDesc(payment.getProductDesc())
                .status(payment.getStatus().name())
                .build();
    }

    public PaymentApprovalResponse toApprovalResponse(Payment payment) {
        return PaymentApprovalResponse.builder()
                .id(payment.getId())
                .orderNo(payment.getOrderNoValue())
                .payToken(payment.getPayToken())
                .status(payment.getStatus().name())
                .mode(payment.getMode())
                .approvalTime(payment.getApprovalTime())
                .stateMsg(payment.getStateMsg())
                .amount(payment.getAmount())
                .discountedAmount(payment.getDiscountedAmount())
                .paidAmount(payment.getPaidAmount())
                .payMethod(payment.getPayMethod())
                .transactionId(payment.getTransactionId())
                .build();
    }

    public PaymentStatusResponse toStatusResponse(TossPaymentStatusResponse statusResponse) {
        PaymentStatusResponse.PaymentStatusResponseBuilder builder = PaymentStatusResponse.builder()
                .payToken(statusResponse.getPayToken())
                .orderNo(statusResponse.getOrderNo())
                .payStatus(statusResponse.getPayStatus())
                .payMethod(statusResponse.getPayMethod())
                .mode(statusResponse.getMode())
                .amount(statusResponse.getAmount())
                .discountedAmount(statusResponse.getDiscountedAmount())
                .discountAmountV2(statusResponse.getDiscountAmountV2())
                .paidPointV2(statusResponse.getPaidPointV2())
                .paidAmount(statusResponse.getPaidAmount())
                .refundableAmount(statusResponse.getRefundableAmount())
                .amountTaxFree(statusResponse.getAmountTaxFree())
                .amountTaxable(statusResponse.getAmountTaxable())
                .amountVat(statusResponse.getAmountVat())
                .amountServiceFee(statusResponse.getAmountServiceFee())
                .disposableCupDeposit(statusResponse.getDisposableCupDeposit())
                .accountBankCode(statusResponse.getAccountBankCode())
                .accountBankName(statusResponse.getAccountBankName())
                .accountNumber(statusResponse.getAccountNumber())
                .createdTs(statusResponse.getCreatedTs())
                .paidTs(statusResponse.getPaidTs());

        if (statusResponse.getCard() != null) {
            TossPaymentStatusResponse.CardInfo card = statusResponse.getCard();
            builder.card(CardInfo.builder()
                    .noInterest(card.getNoInterest())
                    .spreadOut(card.getSpreadOut())
                    .cardAuthorizationNo(card.getCardAuthorizationNo())
                    .cardMethodType(card.getCardMethodType())
                    .cardUserType(card.getCardUserType())
                    .cardNumber(card.getCardNumber())
                    .cardBinNumber(card.getCardBinNumber())
                    .cardNum4Print(card.getCardNum4Print())
                    .salesCheckLinkUrl(card.getSalesCheckLinkUrl())
                    .cardCompanyName(card.getCardCompanyName())
                    .cardCompanyCode(card.getCardCompanyCode())
                    .build());
        }

        if (statusResponse.getTransactions() != null && !statusResponse.getTransactions().isEmpty()) {
            builder.transactions(statusResponse.getTransactions().stream()
                    .map(tx -> TransactionInfo.builder()
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

    public PaymentHistoryResponse toHistoryResponse(Payment payment, boolean isAdmin) {
        PaymentHistoryResponse.PaymentHistoryResponseBuilder builder = PaymentHistoryResponse.builder()
                .id(payment.getId())
                .orderNo(payment.getOrderNoValue())
                .productDesc(payment.getProductDesc())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .payMethod(payment.getPayMethod())
                .createdAt(payment.getCreatedAt());
        
        if (isAdmin) {
            builder.transactionId(payment.getTransactionId());
        }
        
        if (payment.getPaidTs() != null && !payment.getPaidTs().isEmpty()) {
            try {
                builder.paidTs(LocalDateTime.parse(payment.getPaidTs(), DATE_TIME_FORMATTER));
            } catch (Exception e) {
                log.warn("paidTs 파싱 실패: paidTs={}, paymentId={}", payment.getPaidTs(), payment.getId(), e);
            }
        }
        
        return builder.build();
    }

    public PaymentDetailResponse toDetailResponse(Payment payment, boolean isAdmin) {
        PaymentDetailResponse.PaymentDetailResponseBuilder builder = PaymentDetailResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId())
                .orderNo(payment.getOrderNoValue())
                .productDesc(payment.getProductDesc())
                .amount(payment.getAmount())
                .amountTaxFree(payment.getAmountTaxFree())
                .amountTaxable(payment.getAmountTaxable())
                .amountVat(payment.getAmountVat())
                .amountServiceFee(payment.getAmountServiceFee())
                .disposableCupDeposit(payment.getDisposableCupDeposit())
                .status(payment.getStatus().name())
                .payMethod(payment.getPayMethod())
                .discountedAmount(payment.getDiscountedAmount())
                .paidAmount(payment.getPaidAmount())
                .paidTs(payment.getPaidTs())
                .mode(payment.getMode())
                .approvalTime(payment.getApprovalTime())
                .stateMsg(payment.getStateMsg())
                .accountBankCode(payment.getAccountBankCode())
                .accountBankName(payment.getAccountBankName())
                .accountNumber(payment.getAccountNumber())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .expiredTime(payment.getExpiredTime());
        
        if (isAdmin) {
            builder.transactionId(payment.getTransactionId());
        }
        
        if (payment.getCardCompanyName() != null || payment.getCardCompanyCode() != null) {
            builder.card(CardInfo.builder()
                    .cardCompanyName(payment.getCardCompanyName())
                    .cardCompanyCode(payment.getCardCompanyCode())
                    .cardAuthorizationNo(payment.getCardAuthorizationNo())
                    .spreadOut(payment.getSpreadOut())
                    .noInterest(payment.getNoInterest())
                    .cardMethodType(payment.getCardMethodType())
                    .cardUserType(payment.getCardUserType())
                    .cardBinNumber(payment.getCardBinNumber())
                    .cardNum4Print(payment.getCardNum4Print())
                    .salesCheckLinkUrl(payment.getSalesCheckLinkUrl())
                    .build());
        }
        
        return builder.build();
    }

    private Integer convertToInteger(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(0, AMOUNT_ROUNDING_MODE).intValue();
    }
}
