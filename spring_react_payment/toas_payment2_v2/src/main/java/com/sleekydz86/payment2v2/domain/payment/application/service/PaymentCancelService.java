package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.payment.application.dto.CancelPaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CancelPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.CancelPaymentUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.PaymentGatewayPort;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentCancelRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentCancelResponse;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class PaymentCancelService implements CancelPaymentUseCase {

    private static final String LOG_PAYMENT_KEY = "paymentKey";
    private static final String LOG_OPERATION = "operation";

    private final PaymentGatewayPort paymentGatewayPort;

    @Override
    @Transactional
    public CancelPaymentResponse cancelPayment(CancelPaymentCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_PAYMENT_KEY, command.getPaymentKey() != null ? command.getPaymentKey() : "알수없음",
                LOG_OPERATION, "cancelPayment"), () -> {
                    log.info("결제 취소 요청: paymentKey={}, cancelReason={}",
                            command.getPaymentKey(), command.getCancelReason());

                    TossPaymentCancelRequest request = toTossCancelRequest(command);
                    TossPaymentCancelResponse response = callTossCancelApi(command.getPaymentKey(), request);

                    return toCancelResponse(response);
                });
    }

    private TossPaymentCancelRequest toTossCancelRequest(CancelPaymentCommand command) {
        TossPaymentCancelRequest.TossPaymentCancelRequestBuilder builder = TossPaymentCancelRequest.builder()
                .cancelReason(command.getCancelReason())
                .cancelAmount(command.getCancelAmount())
                .taxFreeAmount(command.getTaxFreeAmount())
                .currency(command.getCurrency());

        if (command.getRefundReceiveAccount() != null) {
            TossPaymentCancelRequest.RefundReceiveAccount refundAccount =
                    TossPaymentCancelRequest.RefundReceiveAccount.builder()
                            .bank(command.getRefundReceiveAccount().getBank())
                            .accountNumber(command.getRefundReceiveAccount().getAccountNumber())
                            .holderName(command.getRefundReceiveAccount().getHolderName())
                            .build();
            builder.refundReceiveAccount(refundAccount);
        }

        return builder.build();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Retry(name = "tossPayment")
    @CircuitBreaker(name = "tossPayment", fallbackMethod = "tossCancelFallback")
    private TossPaymentCancelResponse callTossCancelApi(String paymentKey, TossPaymentCancelRequest request) {
        return TossPaymentExceptionHandler.handleTossApiCall("결제 취소", paymentKey, () -> {
            TossPaymentCancelResponse response = paymentGatewayPort.cancelPayment(paymentKey, request);
            return TossPaymentExceptionHandler.validateTossResponse("결제 취소", paymentKey, response,
                    new TossPaymentExceptionHandler.TossResponseValidator<TossPaymentCancelResponse>() {
                        @Override
                        public boolean isSuccess(TossPaymentCancelResponse response) {
                            return response != null && "CANCELED".equals(response.getStatus());
                        }

                        @Override
                        public String getErrorMessage(TossPaymentCancelResponse response) {
                            return response != null ? "결제 취소 실패" : "응답이 null입니다";
                        }

                        @Override
                        public ErrorCode getErrorCode() {
                            return ErrorCode.PAYMENT_REFUND_FAILED;
                        }
                    });
        });
    }

    private TossPaymentCancelResponse tossCancelFallback(String paymentKey, TossPaymentCancelRequest request, Exception e) {
        log.error("토스페이먼츠 결제 취소 API Circuit Breaker 활성화: paymentKey={}", paymentKey, e);
        throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR,
                "토스페이먼츠 서비스가 일시적으로 사용할 수 없습니다.", e);
    }

    private CancelPaymentResponse toCancelResponse(TossPaymentCancelResponse response) {
        List<CancelPaymentResponse.CancelInfo> cancels = null;
        if (response.getCancels() != null) {
            cancels = response.getCancels().stream()
                    .map(cancel -> CancelPaymentResponse.CancelInfo.builder()
                            .transactionKey(cancel.getTransactionKey())
                            .cancelReason(cancel.getCancelReason())
                            .cancelAmount(cancel.getCancelAmount())
                            .canceledAt(cancel.getCanceledAt())
                            .cancelStatus(cancel.getCancelStatus())
                            .build())
                    .collect(Collectors.toList());
        }

        return CancelPaymentResponse.builder()
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

