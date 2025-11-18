package com.sleekydz86.payment2v2.domain.member.model.valueobject;

import com.sleekydz86.payment2v2.global.constants.ValidationConstants;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
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
public class MemberName {
    @Column(name = "name", nullable = false, length = ValidationConstants.MAX_NAME_LENGTH)
    private String value;

    private MemberName(String value) {
        validate(value);
        this.value = value.trim();
    }

    public static MemberName of(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이름은 필수입니다.");
        }
        return new MemberName(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이름은 필수입니다.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > ValidationConstants.MAX_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("이름은 %d자를 초과할 수 없습니다.", ValidationConstants.MAX_NAME_LENGTH));
        }
    }

    public String getValue() {
        return value;
    }
}
