package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.PurchaseConfirmRequest;
import com.sleekydz86.toaspayment.domain.order.Order;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import com.sleekydz86.toaspayment.domain.order.PaymentMethod;
import com.sleekydz86.toaspayment.domain.order.valueobject.Money;
import com.sleekydz86.toaspayment.domain.order.valueobject.OrderId;
import com.sleekydz86.toaspayment.domain.payment.PaymentGateway;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLog;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLogRepository;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLogType;
import com.sleekydz86.toaspayment.exception.BadRequestException;
import com.sleekydz86.toaspayment.infrastructure.external.TossPaymentException;
import com.sleekydz86.toaspayment.infrastructure.external.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmPurchaseUseCase {
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentLogRepository paymentLogRepository;

    @Transactional
    public void execute(PurchaseConfirmRequest request) {
        OrderId orderId = OrderId.of(request.orderId());
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BadRequestException("주문을 찾을 수 없습니다."));

        Money requestAmount = Money.of(request.amount());
        if (!order.getFinalAmount().equalsValue(requestAmount.toInteger())) {
            throw new BadRequestException("결제 금액이 일치하지 않습니다.");
        }

        TossPaymentResponse response;
        try {
            response = paymentGateway.confirmPayment(
                    request.paymentKey(),
                    request.orderId(),
                    request.amount()
            );
        } catch (TossPaymentException e) {
            handlePaymentFailure(order, orderId, requestAmount, e);
            throw new com.sleekydz86.toaspayment.exception.TossPaymentException(
                    "결제 승인에 실패했습니다: " + e.getMessage(),
                    org.springframework.http.HttpStatus.valueOf(e.getStatusCode())
            );
        } catch (Exception e) {
            handlePaymentError(order, orderId, requestAmount, e);
            throw new com.sleekydz86.toaspayment.exception.TossPaymentException(
                    "결제 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        validatePaymentResponse(response, requestAmount);
        savePaymentSuccess(order, orderId, request, response);
    }

    private void savePaymentSuccess(Order order, OrderId orderId, PurchaseConfirmRequest request, TossPaymentResponse response) {
        PaymentMethod paymentMethod = mapPaymentMethod(response.method());
        String paymentKeyFromResponse = response.paymentKey();
        String originalOrderId = paymentKeyFromResponse != null && !paymentKeyFromResponse.isBlank() 
            ? paymentKeyFromResponse 
            : null;
        
        log.info("토스 응답 정보 - paymentKey: {}, orderId: {}, originalOrderId 저장값: {}", 
            paymentKeyFromResponse, response.orderId(), originalOrderId);
        
        order.completePayment(request.paymentKey(), paymentMethod, originalOrderId);
        orderRepository.save(order);
        
        log.info("결제 승인 완료 - 주문 ID: {}, 원본 주문번호: {}", orderId, originalOrderId);

        Money requestAmount = Money.of(request.amount());
        PaymentLog successLog = PaymentLog.create(
                orderId.toString(),
                order.getMemberId(),
                PaymentLogType.PAYMENT_SUCCESS,
                "결제 성공 - 금액: " + requestAmount.toInteger() + "원, 결제수단: " + paymentMethod
        );
        paymentLogRepository.save(successLog);

        log.info("결제 승인 완료 - 주문 ID: {}", orderId);
    }

    private void handlePaymentFailure(Order order, OrderId orderId, Money requestAmount, TossPaymentException e) {
        log.error("토스 페이먼츠 결제 승인 실패 - 주문 ID: {}, 오류: {}", orderId, e.getMessage());
        order.abort();
        orderRepository.save(order);

        PaymentLog failLog = PaymentLog.create(
                orderId.toString(),
                order.getMemberId(),
                PaymentLogType.PAYMENT_FAILED,
                "토스 페이먼츠 결제 실패: " + e.getMessage(),
                "상태 코드: " + e.getStatusCode()
        );
        paymentLogRepository.save(failLog);
    }

    private void handlePaymentError(Order order, OrderId orderId, Money requestAmount, Exception e) {
        log.error("결제 승인 처리 중 예상치 못한 오류 발생 - 주문 ID: {}, 오류: {}", orderId, e.getMessage(), e);
        order.abort();
        orderRepository.save(order);

        PaymentLog errorLog = PaymentLog.create(
                orderId.toString(),
                order.getMemberId(),
                PaymentLogType.PAYMENT_ERROR,
                "결제 처리 중 오류 발생: " + e.getMessage()
        );
        paymentLogRepository.save(errorLog);
    }

    private void validatePaymentResponse(TossPaymentResponse response, Money expectedAmount) {
        if (!"DONE".equals(response.status())) {
            throw new BadRequestException("결제가 완료되지 않았습니다. 현재 상태: " + response.status());
        }

        if (response.totalAmount() == null || !response.totalAmount().equals(expectedAmount.toInteger())) {
            throw new BadRequestException("결제 금액이 일치하지 않습니다. 예상 금액: " + expectedAmount.toInteger() + "원, 실제 금액: " + response.totalAmount() + "원");
        }
    }

    private PaymentMethod mapPaymentMethod(String method) {
        if (method == null) {
            return PaymentMethod.CARD;
        }

        return switch (method.toLowerCase()) {
            case "카드", "card" -> PaymentMethod.CARD;
            case "가상계좌", "virtual_account" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "휴대폰", "mobile" -> PaymentMethod.MOBILE;
            case "계좌이체", "bank_transfer" -> PaymentMethod.BANK_TRANSFER;
            case "간편결제", "easy_pay" -> PaymentMethod.EASY_PAY;
            default -> PaymentMethod.CARD;
        };
    }
}

