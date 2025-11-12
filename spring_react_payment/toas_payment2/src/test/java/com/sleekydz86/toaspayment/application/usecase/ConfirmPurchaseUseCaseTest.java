package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.PurchaseConfirmRequest;
import com.sleekydz86.toaspayment.domain.order.Order;
import com.sleekydz86.toaspayment.domain.order.OrderRepository;
import com.sleekydz86.toaspayment.domain.order.OrderStatus;
import com.sleekydz86.toaspayment.domain.order.valueobject.Money;
import com.sleekydz86.toaspayment.domain.order.valueobject.OrderId;
import com.sleekydz86.toaspayment.domain.payment.PaymentGateway;
import com.sleekydz86.toaspayment.exception.BadRequestException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("결제 승인 Use Case 테스트")
class ConfirmPurchaseUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private ConfirmPurchaseUseCase confirmPurchaseUseCase;

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
    }

    @Test
    @DisplayName("결제 승인 성공")
    void confirmPurchase_success() {
        // given
        PurchaseConfirmRequest request = new PurchaseConfirmRequest(
                paymentKey,
                orderId.toString(),
                "예매 티켓",
                50000);

        TossPaymentResponse response = new TossPaymentResponse(
                "tosspayments",
                "2022-11-16",
                paymentKey,
                "DONE",
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
                new ArrayList<>());

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(testOrder));
        when(paymentGateway.confirmPayment(paymentKey, orderId.toString(), 50000)).thenReturn(response);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        confirmPurchaseUseCase.execute(request);

        // then
        verify(orderRepository, times(1)).findByOrderId(orderId);
        verify(paymentGateway, times(1)).confirmPayment(paymentKey, orderId.toString(), 50000);
        verify(orderRepository, times(1)).save(any(Order.class));
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.DONE);
    }

    @Test
    @DisplayName("주문을 찾을 수 없는 경우")
    void confirmPurchase_orderNotFound() {
        // given
        PurchaseConfirmRequest request = new PurchaseConfirmRequest(
                paymentKey,
                orderId.toString(),
                "예매 티켓",
                50000);

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> confirmPurchaseUseCase.execute(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("결제 금액이 일치하지 않는 경우")
    void confirmPurchase_amountMismatch() {
        // given
        PurchaseConfirmRequest request = new PurchaseConfirmRequest(
                paymentKey,
                orderId.toString(),
                "예매 티켓",
                30000);

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(testOrder));

        // when & then
        assertThatThrownBy(() -> confirmPurchaseUseCase.execute(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("결제 금액이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("토스 페이먼츠 결제 승인 실패")
    void confirmPurchase_tossPaymentFailure() {
        // given
        PurchaseConfirmRequest request = new PurchaseConfirmRequest(
                paymentKey,
                orderId.toString(),
                "예매 티켓",
                50000);

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(testOrder));
        when(paymentGateway.confirmPayment(paymentKey, orderId.toString(), 50000))
                .thenThrow(new TossPaymentException("결제 승인 실패", 400));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when & then
        assertThatThrownBy(() -> confirmPurchaseUseCase.execute(request))
                .isInstanceOf(com.sleekydz86.toaspayment.exception.TossPaymentException.class)
                .hasMessageContaining("결제 승인에 실패했습니다");

        verify(orderRepository, times(2)).save(any(Order.class));
        assertThat(testOrder.getStatus()).isEqualTo(OrderStatus.ABORTED);
    }
}
