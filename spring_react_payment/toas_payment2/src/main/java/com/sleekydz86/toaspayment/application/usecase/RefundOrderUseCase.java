package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.RefundRequest;
import com.sleekydz86.toaspayment.domain.order.Order;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import com.sleekydz86.toaspayment.domain.order.valueobject.Money;
import com.sleekydz86.toaspayment.domain.order.valueobject.OrderId;
import com.sleekydz86.toaspayment.domain.payment.PaymentGateway;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLog;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLogRepository;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLogType;
import com.sleekydz86.toaspayment.global.exception.BadRequestException;
import com.sleekydz86.toaspayment.infrastructure.external.TossPaymentException;
import com.sleekydz86.toaspayment.infrastructure.external.dto.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundOrderUseCase {
        private final OrderRepository orderRepository;
        private final PaymentGateway paymentGateway;
        private final PaymentLogRepository paymentLogRepository;
        private static final int REFUND_DEADLINE_DAYS = 14;

        @Transactional
        public void execute(RefundRequest request) {
                OrderId orderId = OrderId.of(request.orderId());
                Order order = orderRepository.findByOrderId(orderId)
                                .orElseThrow(() -> new BadRequestException("주문을 찾을 수 없습니다."));

                if (!order.isRefundable()) {
                        String errorMessage = String.format(
                                        "환불 가능한 주문이 아닙니다. 현재 상태: %s (필요한 상태: DONE), paymentKey: %s",
                                        order.getStatus(),
                                        order.getPaymentKey() != null ? "있음" : "없음");
                        PaymentLog paymentLog = PaymentLog.create(
                                        orderId.toString(),
                                        order.getMemberId(),
                                        PaymentLogType.REFUND_FAILED,
                                        errorMessage);
                        paymentLogRepository.save(paymentLog);
                        log.warn("환불 실패 - 주문 ID: {}, 상태: {}, paymentKey 존재: {}",
                                        orderId, order.getStatus(), order.getPaymentKey() != null);
                        throw new BadRequestException(errorMessage);
                }

                LocalDateTime paymentDate = order.getCreatedAt();
                LocalDateTime now = LocalDateTime.now();
                long daysSincePayment = ChronoUnit.DAYS.between(paymentDate, now);

                if (daysSincePayment > REFUND_DEADLINE_DAYS) {
                        String errorMessage = String.format(
                                        "환불 기한이 지났습니다. 결제일: %s, 경과일: %d일 (기한: %d일)",
                                        paymentDate, daysSincePayment, REFUND_DEADLINE_DAYS);
                        PaymentLog paymentLog = PaymentLog.create(
                                        orderId.toString(),
                                        order.getMemberId(),
                                        PaymentLogType.REFUND_FAILED,
                                        errorMessage);
                        paymentLogRepository.save(paymentLog);
                        log.warn("환불 실패 - 주문 ID: {}, 결제일: {}, 경과일: {}일",
                                        orderId, paymentDate, daysSincePayment);
                        throw new BadRequestException(errorMessage);
                }

                Money refundAmount = Money.of(request.paidAmount());
                order.requestRefund();
                orderRepository.save(order);

                PaymentLog requestLog = PaymentLog.create(
                                orderId.toString(),
                                order.getMemberId(),
                                PaymentLogType.REFUND_REQUESTED,
                                "환불 요청: " + request.refundReason());
                paymentLogRepository.save(requestLog);

                try {
                        TossPaymentResponse response = paymentGateway.refundPayment(
                                        request.paymentKey(),
                                        request.refundReason());

                        validateRefundResponse(response, refundAmount);

                        order.completeRefund();
                        orderRepository.save(order);

                        PaymentLog successLog = PaymentLog.create(
                                        orderId.toString(),
                                        order.getMemberId(),
                                        PaymentLogType.REFUND_SUCCESS,
                                        "환불 완료 - 금액: " + refundAmount.toInteger() + "원");
                        paymentLogRepository.save(successLog);

                        log.info("환불 완료 - 주문 ID: {}, 금액: {}원", orderId, refundAmount.toInteger());
                } catch (TossPaymentException e) {
                        log.error("토스 페이먼츠 환불 실패 - 주문 ID: {}, 오류: {}", orderId, e.getMessage());
                        order.failRefund();
                        orderRepository.save(order);

                        PaymentLog failLog = PaymentLog.create(
                                        orderId.toString(),
                                        order.getMemberId(),
                                        PaymentLogType.REFUND_FAILED,
                                        "토스 페이먼츠 환불 실패: " + e.getMessage(),
                                        "상태 코드: " + e.getStatusCode());
                        paymentLogRepository.save(failLog);

                        throw new com.sleekydz86.toaspayment.global.exception.TossPaymentException(
                                        "환불 처리에 실패했습니다: " + e.getMessage(),
                                        org.springframework.http.HttpStatus.valueOf(e.getStatusCode()));
                } catch (Exception e) {
                        log.error("환불 처리 중 예상치 못한 오류 발생 - 주문 ID: {}, 오류: {}", orderId, e.getMessage(), e);
                        order.failRefund();
                        orderRepository.save(order);

                        PaymentLog errorLog = PaymentLog.create(
                                        orderId.toString(),
                                        order.getMemberId(),
                                        PaymentLogType.PAYMENT_ERROR,
                                        "환불 처리 중 오류 발생: " + e.getMessage());
                        paymentLogRepository.save(errorLog);

                        throw new com.sleekydz86.toaspayment.global.exception.TossPaymentException(
                                        "환불 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
                }
        }

        private void validateRefundResponse(TossPaymentResponse response, Money expectedAmount) {
                if (response.balanceAmount() != null && response.balanceAmount() != 0) {
                        throw new BadRequestException("환불 후 잔액이 남아있습니다. 잔액: " + response.balanceAmount() + "원");
                }

                if (response.cancels() == null || response.cancels().isEmpty()) {
                        throw new BadRequestException("환불 정보를 찾을 수 없습니다.");
                }

                TossPaymentResponse.CancelDto cancel = response.cancels().get(0);

                if (!"CANCELED".equals(response.status())) {
                        throw new BadRequestException("환불 상태가 올바르지 않습니다. 현재 상태: " + response.status());
                }

                if (!"DONE".equals(cancel.cancelStatus())) {
                        throw new BadRequestException("환불이 완료되지 않았습니다. 현재 상태: " + cancel.cancelStatus());
                }

                if (!cancel.cancelAmount().equals(expectedAmount.toInteger())) {
                        throw new BadRequestException("환불 금액이 일치하지 않습니다. 예상 금액: " + expectedAmount.toInteger()
                                        + "원, 실제 금액: " + cancel.cancelAmount() + "원");
                }
        }
}
