package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.payment.application.dto.RefundPaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.RefundPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.RefundPaymentUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.PaymentGatewayPort;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRefundRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRefundResponse;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.model.valueobject.PaymentId;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import com.sleekydz86.payment2v2.domain.payment.application.event.PaymentRefundedEvent;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import com.sleekydz86.payment2v2.global.util.TossPaymentExceptionHandler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class PaymentRefundService implements RefundPaymentUseCase {

    private static final String LOG_PAYMENT_ID = "paymentId";
    private static final String LOG_OPERATION = "operation";

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final com.sleekydz86.payment2v2.domain.payment.application.service.mapper.TossPaymentMapper tossPaymentMapper;
    private final com.sleekydz86.payment2v2.domain.payment.application.service.mapper.PaymentResponseMapper paymentResponseMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentMetricsService paymentMetricsService;

    @Override
    @Transactional
    public RefundPaymentResponse refundPayment(RefundPaymentCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_PAYMENT_ID, command.getPaymentId() != null ? String.valueOf(command.getPaymentId()) : "알수없음",
                LOG_OPERATION, "refundPayment"), () -> {
                    log.info("결제 환불 요청");

                    PaymentId paymentId = PaymentId.of(command.getPaymentId());
                    Payment payment = findPayment(paymentId);

                    try {
                        payment.validateForRefund();
                    } catch (IllegalArgumentException e) {
                        throw new BusinessException(ErrorCode.PAYMENT_NOT_REFUNDABLE, e.getMessage());
                    }
                    try {
                        payment.validateRefundAmount(command.getAmount());
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        if (e instanceof IllegalStateException) {
                            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
                        }
                        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
                    }

                    TossPaymentRefundRequest refundRequest = tossPaymentMapper.toRefundRequest(command, payment);
                    TossPaymentRefundResponse refundResponse = callTossRefundApi(refundRequest,
                            payment.getOrderNoValue());

                    return updatePaymentWithRefund(payment, refundResponse, command.getRefundNo());
                });
    }

    private Payment findPayment(PaymentId paymentId) {
        return paymentRepository.findById(paymentId.getValue())
                .orElseThrow(() -> {
                    log.warn("결제를 찾을 수 없음: paymentId={}", paymentId.getValue());
                    return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                            String.format("결제 정보를 찾을 수 없습니다. paymentId: %d", paymentId.getValue()));
                });
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Retry(name = "tossPayment")
    @CircuitBreaker(name = "tossPayment", fallbackMethod = "tossRefundFallback")
    private TossPaymentRefundResponse callTossRefundApi(TossPaymentRefundRequest refundRequest, String orderNo) {
        return TossPaymentExceptionHandler.handleTossApiCall("결제 환불", orderNo, () -> {
            TossPaymentRefundResponse response = paymentGatewayPort.refundPayment(refundRequest);
            return TossPaymentExceptionHandler.validateTossResponse("결제 환불", orderNo, response,
                    new TossPaymentExceptionHandler.TossResponseValidator<TossPaymentRefundResponse>() {
                        @Override
                        public boolean isSuccess(TossPaymentRefundResponse response) {
                            return response.isSuccess();
                        }

                        @Override
                        public String getErrorMessage(TossPaymentRefundResponse response) {
                            return response.getMsg();
                        }

                        @Override
                        public ErrorCode getErrorCode() {
                            return ErrorCode.PAYMENT_REFUND_FAILED;
                        }
                    });
        });
    }

    private TossPaymentRefundResponse tossRefundFallback(TossPaymentRefundRequest refundRequest, String orderNo,
            Exception e) {
        log.error("토스페이먼츠 결제 환불 API Circuit Breaker 활성화: orderNo={}", orderNo, e);
        throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, 
                "토스페이먼츠 서비스가 일시적으로 사용할 수 없습니다.", e);
    }

    @Transactional
    @CacheEvict(value = { "paymentHistory", "paymentDetail" }, allEntries = true)
    private RefundPaymentResponse updatePaymentWithRefund(Payment payment, TossPaymentRefundResponse refundResponse,
            String refundNo) {
        payment.refund(refundNo, refundResponse.getRefundableAmount(), refundResponse.getRefundedAmount(),
                refundResponse.getApprovalTime(), refundResponse.getTransactionId());
        payment = paymentRepository.save(payment);

        log.info("결제 환불 완료: paymentId={}, orderNo={}, refundNo={}, refundedAmount={}",
                payment.getId(), payment.getOrderNoValue(), refundNo, refundResponse.getRefundedAmount());

        RefundPaymentResponse response = paymentResponseMapper.toRefundResponse(payment, refundResponse);

        publishPaymentRefundedEvent(payment, refundNo, refundResponse);
        paymentMetricsService.recordPaymentRefunded();

        return response;
    }

    private void publishPaymentRefundedEvent(Payment payment, String refundNo,
            TossPaymentRefundResponse refundResponse) {
        BigDecimal refundedAmount = refundResponse.getRefundedAmount() != null
                ? BigDecimal.valueOf(refundResponse.getRefundedAmount())
                : null;
        eventPublisher.publishEvent(new PaymentRefundedEvent(this, payment.getId(),
                payment.getOrderNoValue(), refundNo, refundedAmount));
    }
}

