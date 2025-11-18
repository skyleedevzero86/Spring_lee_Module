package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.member.model.valueobject.MemberId;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CreatePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.application.port.in.CreatePaymentUseCase;
import com.sleekydz86.payment2v2.domain.payment.application.port.out.PaymentGatewayPort;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentRequest;
import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentResponse;
import com.sleekydz86.payment2v2.domain.payment.model.Payment;
import com.sleekydz86.payment2v2.domain.payment.model.valueobject.OrderNo;
import com.sleekydz86.payment2v2.domain.payment.port.out.PaymentRepository;
import com.sleekydz86.payment2v2.domain.payment.application.event.PaymentCreatedEvent;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import com.sleekydz86.payment2v2.global.util.LoggingUtil;
import com.sleekydz86.payment2v2.global.util.TossPaymentExceptionHandler;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class PaymentCreationService implements CreatePaymentUseCase {

    private static final String LOG_USER_ID = "userId";
    private static final String LOG_ORDER_NO = "orderNo";
    private static final String LOG_OPERATION = "operation";

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final com.sleekydz86.payment2v2.domain.payment.application.service.mapper.TossPaymentMapper tossPaymentMapper;
    private final com.sleekydz86.payment2v2.domain.payment.application.service.mapper.PaymentResponseMapper paymentResponseMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentMetricsService paymentMetricsService;

    @Override
    @Transactional
    @Timed(value = "payment.create", description = "결제 생성 시간")
    public PaymentResponse createPayment(CreatePaymentCommand command) {
        return LoggingUtil.executeWithContext(Map.of(
                LOG_USER_ID, command.getUserId() != null ? String.valueOf(command.getUserId()) : "알수없음",
                LOG_ORDER_NO, command.getOrderNo() != null ? command.getOrderNo() : "알수없음",
                LOG_OPERATION, "createPayment"), () -> {
                    log.info("결제 생성 요청");

                    MemberId userId = MemberId.of(command.getUserId());
                    OrderNo orderNo = OrderNo.of(command.getOrderNo());

                    validateOrderNoNotExists(orderNo);

                    Payment payment = createPaymentEntity(command, userId, orderNo);
                    payment = paymentRepository.save(payment);

                    TossPaymentRequest tossRequest = tossPaymentMapper.toTossRequest(command);
                    TossPaymentResponse tossResponse = callTossPaymentApi(tossRequest, orderNo.getValue());

                    PaymentResponse response = updatePaymentWithTossResponse(payment, tossResponse);

                    publishPaymentCreatedEvent(payment);
                    paymentMetricsService.recordPaymentCreated();

                    return response;
                });
    }

    private void validateOrderNoNotExists(OrderNo orderNo) {
        if (paymentRepository.existsByOrderNo(orderNo.getValue())) {
            log.warn("이미 사용된 주문번호: {}", orderNo.getValue());
            throw new BusinessException(ErrorCode.DUPLICATE_ORDER_NO);
        }
    }

    private Payment createPaymentEntity(CreatePaymentCommand command, MemberId userId, OrderNo orderNo) {
        return Payment.builder()
                .userId(userId.getValue())
                .orderNo(orderNo.getValue())
                .productDesc(command.getProductDesc())
                .amount(command.getAmount())
                .amountTaxFree(command.getAmountTaxFree())
                .amountTaxable(command.getAmountTaxable())
                .amountVat(command.getAmountVat())
                .amountServiceFee(command.getAmountServiceFee())
                .disposableCupDeposit(command.getDisposableCupDeposit())
                .retUrl(command.getRetUrl())
                .retCancelUrl(command.getRetCancelUrl())
                .retAppScheme(command.getRetAppScheme())
                .resultCallback(command.getResultCallback())
                .callbackVersion(command.getCallbackVersion())
                .expiredTime(command.getExpiredTime())
                .build();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Retry(name = "tossPayment")
    @CircuitBreaker(name = "tossPayment", fallbackMethod = "tossPaymentFallback")
    private TossPaymentResponse callTossPaymentApi(TossPaymentRequest tossRequest, String orderNo) {
        return TossPaymentExceptionHandler.handleTossApiCall("결제 생성", orderNo, () -> {
            TossPaymentResponse response = paymentGatewayPort.createPayment(tossRequest);
            return TossPaymentExceptionHandler.validateTossResponse("결제 생성", orderNo, response,
                    new TossPaymentExceptionHandler.TossResponseValidator<TossPaymentResponse>() {
                        @Override
                        public boolean isSuccess(TossPaymentResponse response) {
                            return response.isSuccess();
                        }

                        @Override
                        public String getErrorMessage(TossPaymentResponse response) {
                            return response.getMsg();
                        }

                        @Override
                        public ErrorCode getErrorCode() {
                            return ErrorCode.TOSS_PAYMENT_CREATE_FAILED;
                        }
                    });
        });
    }

    private TossPaymentResponse tossPaymentFallback(TossPaymentRequest tossRequest, String orderNo, Exception e) {
        log.error("토스페이먼츠 API Circuit Breaker 활성화: orderNo={}", orderNo, e);
        throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR,
                "토스페이먼츠 서비스가 일시적으로 사용할 수 없습니다.", e);
    }

    @Transactional
    @CacheEvict(value = { "paymentHistory", "paymentDetail" }, allEntries = true)
    private PaymentResponse updatePaymentWithTossResponse(Payment payment, TossPaymentResponse tossResponse) {
        payment.updateCheckoutInfo(tossResponse.getCheckoutPage(), tossResponse.getPayToken());
        payment = paymentRepository.save(payment);

        log.info("결제 생성 완료: paymentId={}, orderNo={}", payment.getId(), payment.getOrderNoValue());
        return paymentResponseMapper.toResponse(payment);
    }

    private void publishPaymentCreatedEvent(Payment payment) {
        eventPublisher.publishEvent(new PaymentCreatedEvent(this, payment.getId(),
                payment.getOrderNoValue(), payment.getUserId(), payment.getProductDesc()));
    }
}
