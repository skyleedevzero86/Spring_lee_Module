package com.sleekydz86.payment2v2.domain.payment.model.valueobject;

import com.sleekydz86.payment2v2.global.constants.ValidationConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money 값 객체 테스트")
class MoneyTest {

    @Test
    @DisplayName("유효한 금액으로 Money 객체를 생성할 수 있다")
    void 유효한_금액으로_Money_객체를_생성할_수_있다() {
        // given
        BigDecimal value = new BigDecimal("10000");

        // when
        Money money = Money.of(value);

        // then
        assertThat(money.getValue()).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("null 값으로 Money 객체를 생성하면 예외가 발생한다")
    void null_값으로_Money_객체를_생성하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Money.of(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("최소 금액 미만으로 Money 객체를 생성하면 예외가 발생한다")
    void 최소_금액_미만으로_Money_객체를_생성하면_예외가_발생한다() {
        // given
        BigDecimal value = BigDecimal.ZERO;

        // when & then
        assertThatThrownBy(() -> Money.of(value))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("최대 금액 초과로 Money 객체를 생성하면 예외가 발생한다")
    void 최대_금액_초과로_Money_객체를_생성하면_예외가_발생한다() {
        // given
        BigDecimal value = new BigDecimal(ValidationConstants.MAX_AMOUNT + 1);

        // when & then
        assertThatThrownBy(() -> Money.of(value))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("0원 Money 객체를 생성할 수 있다")
    void 영원_Money_객체를_생성할_수_있다() {
        // given
        Money zero = Money.zero();

        // then
        assertThat(zero.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("금액 덧셈이 정상적으로 동작한다")
    void 금액_덧셈이_정상적으로_동작한다() {
        // given
        Money money1 = Money.of(new BigDecimal("10000"));
        Money money2 = Money.of(new BigDecimal("5000"));

        // when
        Money result = money1.add(money2);

        // then
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("15000.00"));
    }

    @Test
    @DisplayName("금액 뺄셈이 정상적으로 동작한다")
    void 금액_뺄셈이_정상적으로_동작한다() {
        // given
        Money money1 = Money.of(new BigDecimal("10000"));
        Money money2 = Money.of(new BigDecimal("3000"));

        // when
        Money result = money1.subtract(money2);

        // then
        assertThat(result.getValue()).isEqualByComparingTo(new BigDecimal("7000.00"));
    }

    @Test
    @DisplayName("금액 비교가 정상적으로 동작한다")
    void 금액_비교가_정상적으로_동작한다() {
        // given
        Money money1 = Money.of(new BigDecimal("10000"));
        Money money2 = Money.of(new BigDecimal("5000"));
        Money money3 = Money.of(new BigDecimal("10000"));

        // when
        boolean isGreater = money1.isGreaterThan(money2);
        boolean isLess = money2.isLessThan(money1);
        boolean isEqual = money1.getValue().compareTo(money3.getValue()) == 0;

        // then
        assertThat(isGreater).isTrue();
        assertThat(isLess).isTrue();
        assertThat(isEqual).isTrue();
    }

    @Test
    @DisplayName("금액을 Integer로 변환할 수 있다")
    void 금액을_Integer로_변환할_수_있다() {
        // given
        Money money = Money.of(new BigDecimal("10000.50"));

        // when
        Integer integerValue = money.toInteger();

        // then
        assertThat(integerValue).isEqualTo(10000);
    }

    @Test
    @DisplayName("소수점이 있는 금액을 정상적으로 처리한다")
    void 소수점이_있는_금액을_정상적으로_처리한다() {
        // given
        BigDecimal value = new BigDecimal("12345.678");

        // when
        Money money = Money.of(value);

        // then
        assertThat(money.getValue()).isEqualByComparingTo(new BigDecimal("12345.68"));
    }
}
