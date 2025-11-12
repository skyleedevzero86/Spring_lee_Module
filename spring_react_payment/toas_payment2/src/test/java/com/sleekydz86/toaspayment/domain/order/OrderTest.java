package com.sleekydz86.toaspayment.domain.order;

import com.sleekydz86.toaspayment.domain.order.valueobject.Money;
import com.sleekydz86.toaspayment.domain.order.valueobject.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order 도메인 테스트")
class OrderTest {

    private OrderId orderId;
    private Money amount;
    private Long memberId;
    private String orderName;

    @BeforeEach
    void setUp() {
        orderId = OrderId.generate();
        amount = Money.of(50000);
        memberId = 1L;
        orderName = "예매 티켓";
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() {
        //given & when
        Order order = Order.create(orderId, orderName, memberId, amount);

        //then
        assertThat(order.getOrderId()).isEqualTo(orderId);
        assertThat(order.getOrderName()).isEqualTo(orderName);
        assertThat(order.getMemberId()).isEqualTo(memberId);
        assertThat(order.getFinalAmount()).isEqualTo(amount);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.isPending()).isTrue();
    }

    @Test
    @DisplayName("결제 완료 처리 성공")
    void completePayment_success() {
        //given
        Order order = Order.create(orderId, orderName, memberId, amount);
        String paymentKey = "test_payment_key";
        PaymentMethod paymentMethod = PaymentMethod.CARD;

        //when
        order.completePayment(paymentKey, paymentMethod);

        //then
        assertThat(order.getPaymentKey()).isEqualTo(paymentKey);
        assertThat(order.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DONE);
        assertThat(order.isDone()).isTrue();
    }

    @Test
    @DisplayName("결제 완료 처리 실패 - PENDING 상태가 아닌 경우")
    void completePayment_fail_wrongStatus() {
        //given
        Order order = Order.create(orderId, orderName, memberId, amount);
        order.completePayment("payment_key", PaymentMethod.CARD);
        order.requestRefund();

        //when & then
        assertThatThrownBy(() -> order.completePayment("payment_key2", PaymentMethod.CARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결제 완료할 수 없는 주문 상태입니다");
    }

    @Test
    @DisplayName("환불 요청 성공")
    void requestRefund_success() {
        //given
        Order order = Order.create(orderId, orderName, memberId, amount);
        order.completePayment("payment_key", PaymentMethod.CARD);

        //when
        order.requestRefund();

        //then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUND_REQUESTED);
        assertThat(order.isRefundable()).isFalse();
    }

    @Test
    @DisplayName("환불 요청 실패 - DONE 상태가 아닌 경우")
    void requestRefund_fail_wrongStatus() {
        //given
        Order order = Order.create(orderId, orderName, memberId, amount);

        //when & then
        assertThatThrownBy(() -> order.requestRefund())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("환불 가능한 주문이 아닙니다");
    }

    @Test
    @DisplayName("환불 완료 처리 성공")
    void completeRefund_success() {
        //given
        Order order = Order.create(orderId, orderName, memberId, amount);
        order.completePayment("payment_key", PaymentMethod.CARD);
        order.requestRefund();

        //when
        order.completeRefund();

        //then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("환불 완료 처리 실패 - REFUND_REQUESTED 상태가 아닌 경우")
    void completeRefund_fail_wrongStatus() {
        //given
        Order order = Order.create(orderId, orderName, memberId, amount);
        order.completePayment("payment_key", PaymentMethod.CARD);

        //when & then
        assertThatThrownBy(() -> order.completeRefund())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("환불 완료할 수 없는 주문 상태입니다");
    }

    @Test
    @DisplayName("환불 실패 처리")
    void failRefund_success() {
        //given
        Order order = Order.create(orderId, orderName, memberId, amount);
        order.completePayment("payment_key", PaymentMethod.CARD);
        order.requestRefund();

        //when
        order.failRefund();

        //then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUND_FAILED);
    }

    @Test
    @DisplayName("주문 중단 처리")
    void abort_success() {
        //given
        Order order = Order.create(orderId, orderName, memberId, amount);

        //when
        order.abort();

        //then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ABORTED);
    }

    @Test
    @DisplayName("환불 가능 여부 확인")
    void isRefundable_check() {
        //given
        Order pendingOrder = Order.create(orderId, orderName, memberId, amount);
        Order doneOrder = Order.create(orderId, orderName, memberId, amount);
        doneOrder.completePayment("payment_key", PaymentMethod.CARD);

        //when & then
        assertThat(pendingOrder.isRefundable()).isFalse();
        assertThat(doneOrder.isRefundable()).isTrue();
    }
}


