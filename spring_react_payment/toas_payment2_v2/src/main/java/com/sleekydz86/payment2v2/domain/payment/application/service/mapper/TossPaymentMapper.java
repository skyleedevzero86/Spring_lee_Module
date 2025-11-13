package com.sleekydz86.payment2v2.domain.payment.application.service.mapper;

import com.sleekydz86.payment2v2.domain.payment.application.dto.CreatePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.RefundPaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRefundRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.global.config.TossPaymentProperties;
import com.sleekydz86.payment2v2.global.constants.PaymentConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class TossPaymentMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(PaymentConstants.DATE_TIME_FORMAT);

    private final String apiKey;

    public TossPaymentMapper(TossPaymentProperties tossPaymentProperties) {
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

    public TossPaymentRefundRequest toRefundRequest(RefundPaymentCommand command, Payment payment) {
        TossPaymentRefundRequest.TossPaymentRefundRequestBuilder builder = TossPaymentRefundRequest.builder()
                .payToken(payment.getPayToken())
                .refundNo(command.getRefundNo())
                .idempotent(command.getIdempotent() != null ? command.getIdempotent() : true)
                .reason(command.getReason());

        if (command.getAmount() != null) {
            builder.amount(convertToInteger(command.getAmount()));
        }
        if (command.getAmountTaxFree() != null) {
            builder.amountTaxFree(convertToInteger(command.getAmountTaxFree()));
        }
        if (command.getAmountTaxable() != null) {
            builder.amountTaxable(convertToInteger(command.getAmountTaxable()));
        }
        if (command.getAmountVat() != null) {
            builder.amountVat(convertToInteger(command.getAmountVat()));
        }
        if (command.getAmountServiceFee() != null) {
            builder.amountServiceFee(convertToInteger(command.getAmountServiceFee()));
        }

        return builder.build();
    }

    private Integer convertToInteger(BigDecimal value) {
        if (value == null) {
            return null;
        }

        BigDecimal rounded = value.setScale(0, PaymentConstants.AMOUNT_ROUNDING_MODE);

        if (rounded.compareTo(BigDecimal.valueOf(PaymentConstants.INTEGER_MAX_VALUE)) > 0) {
            log.error("금액이 Integer 범위를 초과합니다. value={}, maxValue={}", value, PaymentConstants.INTEGER_MAX_VALUE);
            throw new BusinessException(ErrorCode.AMOUNT_EXCEEDS_INTEGER_RANGE,
                    String.format("금액이 Integer 범위를 초과합니다. value: %s", value));
        }

        return rounded.intValue();
    }
}

