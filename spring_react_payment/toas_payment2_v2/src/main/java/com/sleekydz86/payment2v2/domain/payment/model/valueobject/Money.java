package com.sleekydz86.payment2v2.domain.payment.model.valueobject;

import com.sleekydz86.payment2v2.global.constants.ValidationConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Money {
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Column(name = "amount", nullable = false, precision = 19, scale = SCALE)
    private BigDecimal value;

    private Money(BigDecimal value) {
        validate(value);
        this.value = value.setScale(SCALE, ROUNDING_MODE);
    }

    public static Money of(BigDecimal value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "금액은 필수입니다.");
        }
        return new Money(value);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    private void validate(BigDecimal value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "금액은 필수입니다.");
        }
        if (value.compareTo(BigDecimal.valueOf(ValidationConstants.MIN_AMOUNT)) < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("금액은 %d원 이상이어야 합니다.", ValidationConstants.MIN_AMOUNT));
        }
        if (value.compareTo(BigDecimal.valueOf(ValidationConstants.MAX_AMOUNT)) > 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("금액은 %d원을 초과할 수 없습니다.", ValidationConstants.MAX_AMOUNT));
        }
    }

    public Money add(Money other) {
        return new Money(this.value.add(other.value));
    }

    public Money subtract(Money other) {
        return new Money(this.value.subtract(other.value));
    }

    public boolean isGreaterThan(Money other) {
        return this.value.compareTo(other.value) > 0;
    }

    public boolean isLessThan(Money other) {
        return this.value.compareTo(other.value) < 0;
    }

    public Integer toInteger() {
        return value.intValue();
    }

    public BigDecimal getValue() {
        return value;
    }
}
