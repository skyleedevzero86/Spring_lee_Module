package com.sleekydz86.payment2v2.domain.member.model.valueobject;

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
public class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    private static final int MAX_LENGTH = 255;

    @Column(name = "email", nullable = false, unique = true, length = MAX_LENGTH)
    private String value;

    private Email(String value) {
        validate(value);
        this.value = value.toLowerCase().trim();
    }

    public static Email of(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이메일은 필수입니다.");
        }
        return new Email(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이메일은 필수입니다.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("이메일은 %d자를 초과할 수 없습니다.", MAX_LENGTH));
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "올바른 이메일 형식이 아닙니다.");
        }
    }

    public String getValue() {
        return value;
    }
}
