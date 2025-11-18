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

import java.util.regex.Pattern;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class OrderNo {
    private static final Pattern ORDER_NO_PATTERN = Pattern.compile("^[0-9a-zA-Z_\\-:.^@]+$");

    @Column(name = "orderNo", nullable = false, unique = true, length = ValidationConstants.MAX_ORDER_NO_LENGTH)
    private String value;

    private OrderNo(String value) {
        validate(value);
        this.value = value;
    }

    public static OrderNo of(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "주문번호는 필수입니다.");
        }
        return new OrderNo(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "주문번호는 필수입니다.");
        }
        if (value.length() > ValidationConstants.MAX_ORDER_NO_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("주문번호는 %d자를 초과할 수 없습니다.", ValidationConstants.MAX_ORDER_NO_LENGTH));
        }
        if (!ORDER_NO_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "주문번호는 숫자, 영문자, 특수문자 _-:.^@만 사용 가능합니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
