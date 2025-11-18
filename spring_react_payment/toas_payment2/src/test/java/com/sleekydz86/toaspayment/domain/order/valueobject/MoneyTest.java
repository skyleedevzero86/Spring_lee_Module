package com.sleekydz86.toaspayment.domain.order.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money Value Object 테스트")
class MoneyTest {

    @Test
    @DisplayName("Money 생성 성공")
    void createMoney_success() {

        Money money = Money.of(50000);

        assertThat(money.toInteger()).isEqualTo(50000);
    }

    @Test
    @DisplayName("Money 생성 실패 - null 값")
    void createMoney_fail_null() {

        assertThatThrownBy(() -> Money.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("Money 생성 실패 - 0 이하 값")
    void createMoney_fail_zeroOrNegative() {

        assertThatThrownBy(() -> Money.of(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");

        assertThatThrownBy(() -> Money.of(-1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다");
    }

    @Test
    @DisplayName("Money 덧셈 성공")
    void addMoney_success() {

        Money money1 = Money.of(30000);
        Money money2 = Money.of(20000);

        Money result = money1.add(money2);

        assertThat(result.toInteger()).isEqualTo(50000);
    }

    @Test
    @DisplayName("Money 뺄셈 성공")
    void subtractMoney_success() {

        Money money1 = Money.of(50000);
        Money money2 = Money.of(20000);

        Money result = money1.subtract(money2);

        assertThat(result.toInteger()).isEqualTo(30000);
    }

    @Test
    @DisplayName("Money 뺄셈 실패 - 차감할 금액이 더 큰 경우")
    void subtractMoney_fail_insufficientAmount() {

        Money money1 = Money.of(20000);
        Money money2 = Money.of(30000);

        assertThatThrownBy(() -> money1.subtract(money2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("차감할 금액이 현재 금액보다 큽니다");
    }

    @Test
    @DisplayName("Money 비교 - 더 큰지 확인")
    void isGreaterThan_check() {

        Money money1 = Money.of(50000);
        Money money2 = Money.of(30000);

        assertThat(money1.isGreaterThan(money2)).isTrue();
        assertThat(money2.isGreaterThan(money1)).isFalse();
    }

    @Test
    @DisplayName("Money 값 비교")
    void equalsValue_check() {

        Money money = Money.of(50000);

        assertThat(money.equalsValue(50000)).isTrue();
        assertThat(money.equalsValue(30000)).isFalse();
    }
}
