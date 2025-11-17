package com.sleekydz86.toaspayment.domain.order.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Money {
    @Column(name = "final_amount", nullable = false)
    private Integer value;

    private Money(Integer value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }
        this.value = value;
    }

    public static Money of(Integer value) {
        return new Money(value);
    }

    public Money add(Money other) {
        return new Money(this.value + other.value);
    }

    public Money subtract(Money other) {
        if (this.value < other.value) {
            throw new IllegalArgumentException("차감할 금액이 현재 금액보다 큽니다.");
        }
        return new Money(this.value - other.value);
    }

    public boolean isGreaterThan(Money other) {
        return this.value > other.value;
    }

    public boolean equalsValue(Integer other) {
        return this.value.equals(other);
    }

    public Integer toInteger() {
        return value;
    }
}




