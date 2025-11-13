package com.sleekydz86.payment2v2.domain.payment.model.valueobject;

import com.sleekydz86.payment2v2.global.constants.ValidationConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class PaymentId {
    private Long value;

    private PaymentId(Long value) {
        validate(value);
        this.value = value;
    }

    public static PaymentId of(Long value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "결제 ID는 필수입니다.");
        }
        return new PaymentId(value);
    }

    private void validate(Long value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "결제 ID는 필수입니다.");
        }
        if (value < ValidationConstants.MIN_USER_ID) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("유효한 결제 ID가 필요합니다. 최소값: %d", ValidationConstants.MIN_USER_ID));
        }
    }

    public Long getValue() {
        return value;
    }
}

