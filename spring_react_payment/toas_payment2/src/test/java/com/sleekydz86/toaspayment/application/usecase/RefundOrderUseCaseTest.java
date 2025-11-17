package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.RefundRequest;
import com.sleekydz86.toaspayment.domain.order.Order;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import com.sleekydz86.toaspayment.domain.order.OrderStatus;
import com.sleekydz86.toaspayment.domain.order.PaymentMethod;
import com.sleekydz86.toaspayment.domain.order.valueobject.Money;
import com.sleekydz86.toaspayment.domain.order.valueobject.OrderId;
import com.sleekydz86.toaspayment.domain.payment.PaymentGateway;
import com.sleekydz86.toaspayment.global.exception.BadRequestException;
import com.sleekydz86.toaspayment.infrastructure.external.TossPaymentException;
import com.sleekydz86.toaspayment.infrastructure.external.dto.TossPaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("환불 Use Case 테스트")
class RefundOrderUseCaseTest {

        @Mock
        private OrderRepository orderRepository;

        @Mock
        private PaymentGateway paymentGateway;

        @InjectMocks
        private RefundOrderUseCase refundOrderUseCase;

        private Order testOrder;
        private OrderId orderId;
        private Money orderAmount;
        private String paymentKey;

        @BeforeEach
        void setUp() {
                orderId = OrderId.generate();
                orderAmount = Money.of(50000);
                paymentKey = "test_payment_key_12345";
                testOrder = Order.create(orderId, "예매 티켓", 1L, orderAmount);
                testOrder.completePayment(paymentKey, PaymentMethod.CARD);
        }

        @Test
        @DisplayName("환불 성공")
        void refundOrder_success() {
                // given
                RefundRequest request = new RefundRequest(
                                paymentKey,
                                orderId.toString(),
                                "구매자 환불 요청",
                                50000);

                List<TossPaymentResponse.CancelDto> cancels = new ArrayList<>();
                cancels.add(new TossPaymentResponse.CancelDto(
                                "구매자 환불 요청",
                                "2024-01-01T11:00:00+09:00",
                                50000,
                                0,
                                0,
                                50000,
                                0,
                                0,
                                "transaction_key",
                                "receipt_key",
                                "DONE",
                                null));

                TossPaymentResponse response = new TossPaymentResponse(
                                "tosspayments",
                                "2022-11-16",
                                paymentKey,
                                "CANCELED",
                                "transaction_key",
                                orderId.toString(),
                                "예매 티켓",
                                "2024-01-01T10:00:00+09:00",
                                "2024-01-01T10:01:00+09:00",
                                false,
                                false,
                                null,
                                "NORMAL",
                                "KR",
                                "KRW",
                                50000,
                                0,
                                45455,
                                4545,
                                0,
                                0,
                                "카드",
                                cancels);

                when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(testOrder));
                when(paymentGateway.refundPayment(paymentKey, "구매자 환불 요청")).thenReturn(response);
                when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // when
                refundOrderUseCase.execute(request);

                // then
                verify(orderRepository, times(1)).findByOrderId(orderId);
                verify(paymentGateway, times(1)).refundPayment(paymentKey, "구매자 환불 요청");
                verify(orderRepository, times(2)).save(any(Order.class));
                assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);
        }

        @Test
        @DisplayName("주문을 찾을 수 없는 경우")
        void refundOrder_orderNotFound() {
                // given
                RefundRequest request = new RefundRequest(
                                paymentKey,
                                orderId.toString(),
                                "구매자 환불 요청",
                                50000);

                when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> refundOrderUseCase.execute(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("주문을 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("환불 가능한 주문이 아닌 경우")
        void refundOrder_notRefundable() {
                // given
                RefundRequest request = new RefundRequest(
                                paymentKey,
                                orderId.toString(),
                                "구매자 환불 요청",
                                50000);

                Order pendingOrder = Order.create(orderId, "예매 티켓", 1L, orderAmount);
                when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(pendingOrder));

                // when & then
                assertThatThrownBy(() -> refundOrderUseCase.execute(request))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("환불 가능한 주문이 아닙니다.");
        }

        @Test
        @DisplayName("토스 페이먼츠 환불 실패")
        void refundOrder_tossPaymentFailure() {
                // given
                RefundRequest request = new RefundRequest(
                                paymentKey,
                                orderId.toString(),
                                "구매자 환불 요청",
                                50000);

                when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(testOrder));
                when(paymentGateway.refundPayment(paymentKey, "구매자 환불 요청"))
                                .thenThrow(new TossPaymentException("환불 처리 실패", 400));
                when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

                // when & then
                assertThatThrownBy(() -> refundOrderUseCase.execute(request))
                                .isInstanceOf(com.sleekydz86.toaspayment.global.exception.TossPaymentException.class)
                                .hasMessageContaining("환불 처리에 실패했습니다");

                verify(orderRepository, times(2)).save(any(Order.class));
                assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.REFUND_FAILED);
        }
}


