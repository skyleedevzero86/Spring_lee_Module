package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.payment.application.dto.*;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.CashReceiptUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.PaymentGatewayPort;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.*;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import com.sleekydz86.payment2v2.global.util.TossPaymentExceptionHandler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class CashReceiptService implements CashReceiptUseCase {

    private static final String LOG_OPERATION = "operation";

    private final PaymentGatewayPort paymentGatewayPort;

    @Override
    @Transactional
    public CashReceiptResponse issueCashReceipt(IssueCashReceiptCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_OPERATION, "issueCashReceipt"), () -> {
                    log.info("현금영수증 발급 요청: orderId={}, type={}", 
                            command.getOrderId(), command.getType());

                    TossCashReceiptRequest request = TossCashReceiptRequest.builder()
                            .amount(command.getAmount())
                            .orderId(command.getOrderId())
                            .orderName(command.getOrderName())
                            .type(command.getType())
                            .customerIdentityNumber(command.getCustomerIdentityNumber())
                            .taxFreeAmount(command.getTaxFreeAmount())
                            .build();

                    TossCashReceiptResponse response = callTossIssueCashReceiptApi(request);
                    return toCashReceiptResponse(response);
                });
    }

    @Override
    @Transactional
    public CashReceiptResponse cancelCashReceipt(CancelCashReceiptCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_OPERATION, "cancelCashReceipt"), () -> {
                    log.info("현금영수증 취소 요청: receiptKey={}", command.getReceiptKey());

                    TossCashReceiptCancelRequest request = TossCashReceiptCancelRequest.builder()
                            .amount(command.getAmount())
                            .build();

                    TossCashReceiptResponse response = callTossCancelCashReceiptApi(command.getReceiptKey(), request);
                    return toCashReceiptResponse(response);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public CashReceiptListResponse getCashReceipts(String requestDate, Long cursor, Integer limit) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_OPERATION, "getCashReceipts"), () -> {
                    log.info("현금영수증 조회 요청: requestDate={}, cursor={}, limit={}", 
                            requestDate, cursor, limit);

                    TossCashReceiptListResponse response = callTossGetCashReceiptsApi(requestDate, cursor, limit);
                    return toCashReceiptListResponse(response);
                });
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Retry(name = "tossPayment")
    @CircuitBreaker(name = "tossPayment", fallbackMethod = "tossIssueCashReceiptFallback")
    private TossCashReceiptResponse callTossIssueCashReceiptApi(TossCashReceiptRequest request) {
        return TossPaymentExceptionHandler.handleTossApiCall("현금영수증 발급", request.getOrderId(), () -> {
            TossCashReceiptResponse response = paymentGatewayPort.issueCashReceipt(request);
            return TossPaymentExceptionHandler.validateTossResponse("현금영수증 발급", request.getOrderId(), response,
                    new TossPaymentExceptionHandler.TossResponseValidator<TossCashReceiptResponse>() {
                        @Override
                        public boolean isSuccess(TossCashReceiptResponse response) {
                            return response != null;
                        }

                        @Override
                        public String getErrorMessage(TossCashReceiptResponse response) {
                            return response != null && response.getFailure() != null 
                                    ? response.getFailure().getMessage() : "현금영수증 발급 실패";
                        }

                        @Override
                        public ErrorCode getErrorCode() {
                            return ErrorCode.PAYMENT_REFUND_FAILED;
                        }
                    });
        });
    }

    private TossCashReceiptResponse tossIssueCashReceiptFallback(TossCashReceiptRequest request, Exception e) {
        log.error("토스페이먼츠 현금영수증 발급 API Circuit Breaker 활성화: orderId={}", request.getOrderId(), e);
        throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, 
                "토스페이먼츠 서비스가 일시적으로 사용할 수 없습니다.", e);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Retry(name = "tossPayment")
    @CircuitBreaker(name = "tossPayment", fallbackMethod = "tossCancelCashReceiptFallback")
    private TossCashReceiptResponse callTossCancelCashReceiptApi(String receiptKey, TossCashReceiptCancelRequest request) {
        return TossPaymentExceptionHandler.handleTossApiCall("현금영수증 취소", receiptKey, () -> {
            TossCashReceiptResponse response = paymentGatewayPort.cancelCashReceipt(receiptKey, request);
            return TossPaymentExceptionHandler.validateTossResponse("현금영수증 취소", receiptKey, response,
                    new TossPaymentExceptionHandler.TossResponseValidator<TossCashReceiptResponse>() {
                        @Override
                        public boolean isSuccess(TossCashReceiptResponse response) {
                            return response != null;
                        }

                        @Override
                        public String getErrorMessage(TossCashReceiptResponse response) {
                            return response != null && response.getFailure() != null 
                                    ? response.getFailure().getMessage() : "현금영수증 취소 실패";
                        }

                        @Override
                        public ErrorCode getErrorCode() {
                            return ErrorCode.PAYMENT_REFUND_FAILED;
                        }
                    });
        });
    }

    private TossCashReceiptResponse tossCancelCashReceiptFallback(String receiptKey, TossCashReceiptCancelRequest request, Exception e) {
        log.error("토스페이먼츠 현금영수증 취소 API Circuit Breaker 활성화: receiptKey={}", receiptKey, e);
        throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, 
                "토스페이먼츠 서비스가 일시적으로 사용할 수 없습니다.", e);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Retry(name = "tossPayment")
    @CircuitBreaker(name = "tossPayment", fallbackMethod = "tossGetCashReceiptsFallback")
    private TossCashReceiptListResponse callTossGetCashReceiptsApi(String requestDate, Long cursor, Integer limit) {
        return TossPaymentExceptionHandler.handleTossApiCall("현금영수증 조회", requestDate, () -> {
            TossCashReceiptListResponse response = paymentGatewayPort.getCashReceipts(requestDate, cursor, limit);
            return TossPaymentExceptionHandler.validateTossResponse("현금영수증 조회", requestDate, response,
                    new TossPaymentExceptionHandler.TossResponseValidator<TossCashReceiptListResponse>() {
                        @Override
                        public boolean isSuccess(TossCashReceiptListResponse response) {
                            return response != null;
                        }

                        @Override
                        public String getErrorMessage(TossCashReceiptListResponse response) {
                            return "현금영수증 조회 실패";
                        }

                        @Override
                        public ErrorCode getErrorCode() {
                            return ErrorCode.PAYMENT_REFUND_FAILED;
                        }
                    });
        });
    }

    private TossCashReceiptListResponse tossGetCashReceiptsFallback(String requestDate, Long cursor, Integer limit, Exception e) {
        log.error("토스페이먼츠 현금영수증 조회 API Circuit Breaker 활성화: requestDate={}", requestDate, e);
        throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, 
                "토스페이먼츠 서비스가 일시적으로 사용할 수 없습니다.", e);
    }

    private CashReceiptResponse toCashReceiptResponse(TossCashReceiptResponse response) {
        CashReceiptResponse.FailureInfo failureInfo = null;
        if (response.getFailure() != null) {
            failureInfo = CashReceiptResponse.FailureInfo.builder()
                    .code(response.getFailure().getCode())
                    .message(response.getFailure().getMessage())
                    .build();
        }

        return CashReceiptResponse.builder()
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

    private CashReceiptListResponse toCashReceiptListResponse(TossCashReceiptListResponse response) {
        List<CashReceiptResponse> data = null;
        if (response.getData() != null) {
            data = response.getData().stream()
                    .map(this::toCashReceiptResponse)
                    .collect(Collectors.toList());
        }

        return CashReceiptListResponse.builder()
                .hasNext(response.getHasNext())
                .lastCursor(response.getLastCursor())
                .data(data)
                .build();
    }
}

