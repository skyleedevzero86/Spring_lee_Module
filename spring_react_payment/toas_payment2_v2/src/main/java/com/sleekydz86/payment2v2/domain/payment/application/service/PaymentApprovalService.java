package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.payment.application.dto.ApprovePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentApprovalResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.ApprovePaymentUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.PaymentGatewayPort;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentExecuteResponse;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.model.PaymentStatus;
import com.sleekydz86.payment2v2.domain.payment.model.valueobject.OrderNo;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import com.sleekydz86.payment2v2.domain.payment.application.event.PaymentCompletedEvent;
import com.sleekydz86.payment2v2.global.constants.PaymentConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import com.sleekydz86.payment2v2.global.util.TossPaymentExceptionHandler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApprovalService implements ApprovePaymentUseCase {

    private static final String LOG_ORDER_NO = "orderNo";
    private static final String LOG_OPERATION = "operation";

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final com.sleekydz86.payment2v2.domain.payment.application.service.mapper.PaymentResponseMapper paymentResponseMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentMetricsService paymentMetricsService;

    @Override
    @Transactional
    public PaymentApprovalResponse approvePayment(ApprovePaymentCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_ORDER_NO, command.getOrderNo() != null ? command.getOrderNo() : "알수없음",
                LOG_OPERATION, "approvePayment"), () -> {
                    log.info("결제 승인 요청");

                    Payment payment = findPayment(command);
                    try {
                        payment.validateForApproval();
                    } catch (IllegalArgumentException e) {
                        throw new BusinessException(ErrorCode.PAYMENT_NOT_APPROVED, e.getMessage());
                    }
                    String payToken = determinePayToken(command, payment);

                    TossPaymentExecuteResponse executeResponse = callTossExecuteApi(payToken, command.getOrderNo());

                    return updatePaymentWithApproval(payment, executeResponse);
                });
    }

    private Payment findPayment(ApprovePaymentCommand command) {
        if (command.getPayToken() != null && !command.getPayToken().isBlank()) {
            return paymentRepository.findByPayToken(command.getPayToken())
                    .orElseThrow(() -> {
                        log.warn("결제를 찾을 수 없음: payToken={}", command.getPayToken());
                        return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                                String.format("결제 토큰으로 결제 정보를 찾을 수 없습니다. payToken: %s", command.getPayToken()));
                    });
        }

        if (command.getOrderNo() != null && !command.getOrderNo().isBlank()) {
            OrderNo orderNo = OrderNo.of(command.getOrderNo());
            return paymentRepository.findByOrderNo(orderNo.getValue())
                    .orElseThrow(() -> {
                        log.warn("결제를 찾을 수 없음: orderNo={}", orderNo.getValue());
                        return new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                                String.format("주문번호로 결제 정보를 찾을 수 없습니다. orderNo: %s", orderNo.getValue()));
                    });
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                "결제 토큰 또는 주문번호 중 하나는 필수입니다.");
    }

    private String determinePayToken(ApprovePaymentCommand command, Payment payment) {
        if (command.getPayToken() != null && !command.getPayToken().isBlank()) {
            try {
                payment.validatePayToken(command.getPayToken());
            } catch (IllegalArgumentException e) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
            }
            return command.getPayToken();
        }

        if (payment.getPayToken() == null || payment.getPayToken().isBlank()) {
            log.warn("결제 토큰 없음: paymentId={}, orderNo={}", payment.getId(), payment.getOrderNoValue());
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND,
                    String.format("결제 토큰이 없습니다. orderNo: %s", payment.getOrderNoValue()));
        }

        return payment.getPayToken();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Retry(name = "tossPayment")
    @CircuitBreaker(name = "tossPayment", fallbackMethod = "tossExecuteFallback")
    private TossPaymentExecuteResponse callTossExecuteApi(String payToken, String orderNo) {
        return TossPaymentExceptionHandler.handleTossApiCall("결제 승인", orderNo, () -> {
            TossPaymentExecuteRequest executeRequest = TossPaymentExecuteRequest.builder()
                    .payToken(payToken)
                    .orderNo(orderNo)
                    .build();

            TossPaymentExecuteResponse response = paymentGatewayPort.executePayment(executeRequest);
            return TossPaymentExceptionHandler.validateTossResponse("결제 승인", orderNo, response,
                    new TossPaymentExceptionHandler.TossResponseValidator<TossPaymentExecuteResponse>() {
                        @Override
                        public boolean isSuccess(TossPaymentExecuteResponse response) {
                            return response.isSuccess();
                        }

                        @Override
                        public String getErrorMessage(TossPaymentExecuteResponse response) {
                            return response.getMsg();
                        }

                        @Override
                        public ErrorCode getErrorCode() {
                            return ErrorCode.TOSS_PAYMENT_EXECUTE_FAILED;
                        }
                    });
        });
    }

    private TossPaymentExecuteResponse tossExecuteFallback(String payToken, String orderNo, Exception e) {
        log.error("토스페이먼츠 결제 승인 API Circuit Breaker 활성화: orderNo={}", orderNo, e);
        throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, 
                "토스페이먼츠 서비스가 일시적으로 사용할 수 없습니다.", e);
    }

    @Transactional
    private PaymentApprovalResponse updatePaymentWithApproval(Payment payment,
            TossPaymentExecuteResponse executeResponse) {
        updatePaymentWithApprovalData(payment, executeResponse);
        updatePaymentMethodInfo(payment, executeResponse);
        payment = paymentRepository.save(payment);

        log.info("결제 승인 완료: paymentId={}, orderNo={}, transactionId={}",
                payment.getId(), payment.getOrderNoValue(), payment.getTransactionId());

        PaymentApprovalResponse response = paymentResponseMapper.toApprovalResponse(payment);

        publishPaymentCompletedEvent(payment);
        paymentMetricsService.recordPaymentCompleted();

        return response;
    }

    private void updatePaymentWithApprovalData(Payment payment, TossPaymentExecuteResponse executeResponse) {
        payment.approvePayment(
                executeResponse.getMode(),
                executeResponse.getApprovalTime(),
                executeResponse.getStateMsg(),
                executeResponse.getPayMethod(),
                executeResponse.getDiscountedAmount() != null
                        ? BigDecimal.valueOf(executeResponse.getDiscountedAmount())
                        : null,
                executeResponse.getPaidAmount() != null ? BigDecimal.valueOf(executeResponse.getPaidAmount()) : null,
                executeResponse.getTransactionId(),
                executeResponse.getCashReceiptMgtKey());
    }

    private void updatePaymentMethodInfo(Payment payment, TossPaymentExecuteResponse executeResponse) {
        String payMethod = executeResponse.getPayMethod();
        if (payMethod == null) {
            log.debug("결제 수단이 null입니다. paymentId={}", payment.getId());
            return;
        }

        switch (payMethod) {
            case PaymentConstants.PAY_METHOD_CARD -> updateCardInfo(payment, executeResponse);
            case PaymentConstants.PAY_METHOD_TOSS_MONEY -> updateAccountInfo(payment, executeResponse);
            default -> log.debug("지원하지 않는 결제 수단: payMethod={}, paymentId={}", payMethod, payment.getId());
        }
    }

    private void updateCardInfo(Payment payment, TossPaymentExecuteResponse executeResponse) {
        payment.updateApprovalCardInfo(
                executeResponse.getCardCompanyName(),
                executeResponse.getCardCompanyCode(),
                executeResponse.getCardAuthorizationNo(),
                executeResponse.getSpreadOut(),
                executeResponse.getNoInterest(),
                executeResponse.getSalesCheckLinkUrl(),
                executeResponse.getCardMethodType(),
                executeResponse.getCardNumber(),
                executeResponse.getCardUserType(),
                executeResponse.getCardBinNumber(),
                executeResponse.getCardNum4Print());
    }

    private void updateAccountInfo(Payment payment, TossPaymentExecuteResponse executeResponse) {
        payment.updateApprovalAccountInfo(
                executeResponse.getAccountBankCode(),
                executeResponse.getAccountBankName(),
                executeResponse.getAccountNumber());
    }

    private void publishPaymentCompletedEvent(Payment payment) {
        eventPublisher.publishEvent(new PaymentCompletedEvent(this, payment.getId(),
                payment.getOrderNoValue(), payment.getAmount(), payment.getTransactionId()));
    }
}

