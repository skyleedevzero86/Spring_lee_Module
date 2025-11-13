package com.sleekydz86.payment2v2.domain.member.model.valueobject;

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
public class Password {
    private String encodedValue;

    private Password(String encodedValue) {
        if (encodedValue == null || encodedValue.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 필수입니다.");
        }
        this.encodedValue = encodedValue;
    }

    public static Password ofEncoded(String encodedValue) {
        return new Password(encodedValue);
    }

    public static void validateRaw(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 필수입니다.");
        }
        if (rawPassword.length() < ValidationConstants.MIN_PASSWORD_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("비밀번호는 최소 %d자 이상이어야 합니다.", ValidationConstants.MIN_PASSWORD_LENGTH));
        }
    }

    public String getEncodedValue() {
        return encodedValue;
    }
}

