package com.sleekydz86.payment2v2.domain.member.model.valueobject;

import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Password 값 객체 테스트")
class PasswordTest {

    @Test
    @DisplayName("암호화된 비밀번호로 Password 객체를 생성할 수 있다")
    void 암호화된_비밀번호로_Password_객체를_생성할_수_있다() {
        // given
        String encodedPassword = "$2a$10$encodedPasswordHash";

        // when
        Password password = Password.ofEncoded(encodedPassword);

        // then
        assertThat(password.getEncodedValue()).isEqualTo(encodedPassword);
    }

    @Test
    @DisplayName("null 값으로 Password 객체를 생성하면 예외가 발생한다")
    void null_값으로_Password_객체를_생성하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Password.ofEncoded(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("빈 문자열로 Password 객체를 생성하면 예외가 발생한다")
    void 빈_문자열로_Password_객체를_생성하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Password.ofEncoded(""))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        assertThatThrownBy(() -> Password.ofEncoded("   "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("유효한 원본 비밀번호 검증이 통과한다")
    void 유효한_원본_비밀번호_검증이_통과한다() {
        // given
        String validPassword = "password123";

        // when
        Password.validateRaw(validPassword);
    }

    @Test
    @DisplayName("null 값으로 원본 비밀번호 검증하면 예외가 발생한다")
    void null_값으로_원본_비밀번호_검증하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Password.validateRaw(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("빈 문자열로 원본 비밀번호 검증하면 예외가 발생한다")
    void 빈_문자열로_원본_비밀번호_검증하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Password.validateRaw(""))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        assertThatThrownBy(() -> Password.validateRaw("   "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("최소 길이 미만인 원본 비밀번호 검증하면 예외가 발생한다")
    void 최소_길이_미만인_원본_비밀번호_검증하면_예외가_발생한다() {
        // given
        String shortPassword = "1234567";

        // when & then
        assertThatThrownBy(() -> Password.validateRaw(shortPassword))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("최소 길이인 원본 비밀번호 검증이 통과한다")
    void 최소_길이인_원본_비밀번호_검증이_통과한다() {
        // given
        String minLengthPassword = "12345678";

        // when
        Password.validateRaw(minLengthPassword);
    }

    @Test
    @DisplayName("최소 길이보다 긴 원본 비밀번호 검증이 통과한다")
    void 최소_길이보다_긴_원본_비밀번호_검증이_통과한다() {
        // given
        String longPassword = "password123456789";

        // when
        Password.validateRaw(longPassword);
    }
}
