package com.sleekydz86.toaspayment.domain.order.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderId Value Object 테스트")
class OrderIdTest {

    @Test
    @DisplayName("OrderId 생성 성공 - generate")
    void generateOrderId_success() {
        //given & when
        OrderId orderId = OrderId.generate();

        //then
        assertThat(orderId.toString()).isNotNull();
        assertThat(orderId.toString().length()).isGreaterThan(0);
    }

    @Test
    @DisplayName("OrderId 생성 성공 - of")
    void createOrderId_success() {
        //given
        String value = "test-order-id-12345";

        //when
        OrderId orderId = OrderId.of(value);

        //then
        assertThat(orderId.toString()).isEqualTo(value);
    }

    @Test
    @DisplayName("OrderId 생성 실패 - null 값")
    void createOrderId_fail_null() {
        //when & then
        assertThatThrownBy(() -> OrderId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OrderId는 필수입니다");
    }

    @Test
    @DisplayName("OrderId 생성 실패 - 빈 문자열")
    void createOrderId_fail_blank() {
        //when & then
        assertThatThrownBy(() -> OrderId.of(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OrderId는 필수입니다");

        assertThatThrownBy(() -> OrderId.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OrderId는 필수입니다");
    }

    @Test
    @DisplayName("OrderId toString 테스트")
    void toString_test() {
        //given
        String value = "test-order-id";
        OrderId orderId = OrderId.of(value);

        //when
        String result = orderId.toString();

        //then
        assertThat(result).isEqualTo(value);
    }
}


