package com.sleekydz86.payment2v2.domain.payment.model.valueobject;

import com.sleekydz86.payment2v2.global.constants.ValidationConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderNo 값 객체 테스트")
class OrderNoTest {

    @Test
    @DisplayName("유효한 주문번호로 OrderNo 객체를 생성할 수 있다")
    void 유효한_주문번호로_OrderNo_객체를_생성할_수_있다() {

        String orderNo = "ORDER-12345";

        OrderNo orderNoObj = OrderNo.of(orderNo);

        assertThat(orderNoObj.getValue()).isEqualTo(orderNo);
    }

    @Test
    @DisplayName("null 값으로 OrderNo 객체를 생성하면 예외가 발생한다")
    void null_값으로_OrderNo_객체를_생성하면_예외가_발생한다() {

        assertThatThrownBy(() -> OrderNo.of(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("빈 문자열로 OrderNo 객체를 생성하면 예외가 발생한다")
    void 빈_문자열로_OrderNo_객체를_생성하면_예외가_발생한다() {

        assertThatThrownBy(() -> OrderNo.of(""))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        assertThatThrownBy(() -> OrderNo.of("   "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("최대 길이를 초과하는 주문번호로 OrderNo 객체를 생성하면 예외가 발생한다")
    void 최대_길이를_초과하는_주문번호로_OrderNo_객체를_생성하면_예외가_발생한다() {

        String longOrderNo = "A".repeat(ValidationConstants.MAX_ORDER_NO_LENGTH + 1);

        assertThatThrownBy(() -> OrderNo.of(longOrderNo))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("허용된 특수문자가 포함된 주문번호로 OrderNo 객체를 생성할 수 있다")
    void 허용된_특수문자가_포함된_주문번호로_OrderNo_객체를_생성할_수_있다() {

        String[] validOrderNos = {
                "ORDER-123",
                "ORDER_123",
                "ORDER:123",
                "ORDER.123",
                "ORDER^123",
                "ORDER@123",
                "ORDER-123_456:789"
        };

        for (String orderNo : validOrderNos) {

            OrderNo orderNoObj = OrderNo.of(orderNo);

            assertThat(orderNoObj.getValue()).isEqualTo(orderNo);
        }
    }

    @Test
    @DisplayName("허용되지 않은 특수문자가 포함된 주문번호로 OrderNo 객체를 생성하면 예외가 발생한다")
    void 허용되지_않은_특수문자가_포함된_주문번호로_OrderNo_객체를_생성하면_예외가_발생한다() {

        String[] invalidOrderNos = {
                "ORDER 123",
                "ORDER#123",
                "ORDER$123",
                "ORDER%123",
                "ORDER&123",
                "ORDER*123"
        };

        for (String orderNo : invalidOrderNos) {

            assertThatThrownBy(() -> OrderNo.of(orderNo))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Test
    @DisplayName("숫자와 영문자로만 구성된 주문번호로 OrderNo 객체를 생성할 수 있다")
    void 숫자와_영문자로만_구성된_주문번호로_OrderNo_객체를_생성할_수_있다() {

        String orderNo = "ORDER123456";

        OrderNo orderNoObj = OrderNo.of(orderNo);

        assertThat(orderNoObj.getValue()).isEqualTo(orderNo);
    }

    @Test
    @DisplayName("최대 길이 주문번호로 OrderNo 객체를 생성할 수 있다")
    void 최대_길이_주문번호로_OrderNo_객체를_생성할_수_있다() {

        String orderNo = "A".repeat(ValidationConstants.MAX_ORDER_NO_LENGTH);

        OrderNo orderNoObj = OrderNo.of(orderNo);

        assertThat(orderNoObj.getValue()).isEqualTo(orderNo);
    }
}
