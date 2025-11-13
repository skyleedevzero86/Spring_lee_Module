package com.sleekydz86.payment2v2.domain.member.model.valueobject;

import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Email 값 객체 테스트")
class EmailTest {

    @Test
    @DisplayName("유효한 이메일로 Email 객체를 생성할 수 있다")
    void 유효한_이메일로_Email_객체를_생성할_수_있다() {
        // given
        String emailValue = "test@example.com";

        // when
        Email email = Email.of(emailValue);

        // then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("이메일은 소문자로 변환되어 저장된다")
    void 이메일은_소문자로_변환되어_저장된다() {
        // given
        String emailValue = "TEST@EXAMPLE.COM";

        // when
        Email email = Email.of(emailValue);

        // then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("이메일 앞뒤 공백이 제거되어 저장된다")
    void 이메일_앞뒤_공백이_제거되어_저장된다() {
        // given
        String emailValue = "  test@example.com  ";

        // when
        Email email = Email.of(emailValue);

        // then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("null 값으로 Email 객체를 생성하면 예외가 발생한다")
    void null_값으로_Email_객체를_생성하면_예외가_발생한다() {
        // given & when & then
        assertThatThrownBy(() -> Email.of(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("빈 문자열로 Email 객체를 생성하면 예외가 발생한다")
    void 빈_문자열로_Email_객체를_생성하면_예외가_발생한다() {
        // given & when & then
        assertThatThrownBy(() -> Email.of(""))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        assertThatThrownBy(() -> Email.of("   "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("최대 길이를 초과하는 이메일로 Email 객체를 생성하면 예외가 발생한다")
    void 최대_길이를_초과하는_이메일로_Email_객체를_생성하면_예외가_발생한다() {
        // given
        String longEmail = "a".repeat(250) + "@example.com";

        // when & then
        assertThatThrownBy(() -> Email.of(longEmail))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("올바르지 않은 형식의 이메일로 Email 객체를 생성하면 예외가 발생한다")
    void 올바르지_않은_형식의_이메일로_Email_객체를_생성하면_예외가_발생한다() {
        // given
        String[] invalidEmails = {
                "invalid-email",
                "@example.com",
                "test@",
                "test@.com",
                "test..test@example.com",
                "test@example",
                "test @example.com",
                "test@exam ple.com"
        };

        // when & then
        for (String invalidEmail : invalidEmails) {
            assertThatThrownBy(() -> Email.of(invalidEmail))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Test
    @DisplayName("올바른 형식의 이메일로 Email 객체를 생성할 수 있다")
    void 올바른_형식의_이메일로_Email_객체를_생성할_수_있다() {
        // given
        String[] validEmails = {
                "test@example.com",
                "user.name@example.com",
                "user+tag@example.co.kr",
                "user123@example-domain.com",
                "a@b.co"
        };

        // when & then
        for (String validEmail : validEmails) {
            Email email = Email.of(validEmail);
            assertThat(email.getValue()).isEqualTo(validEmail.toLowerCase());
        }
    }

    @Test
    @DisplayName("최대 길이 이메일로 Email 객체를 생성할 수 있다")
    void 최대_길이_이메일로_Email_객체를_생성할_수_있다() {
        // given
        String longEmail = "a".repeat(240) + "@example.com";

        // when
        Email email = Email.of(longEmail);

        // then
        assertThat(email.getValue()).isEqualTo(longEmail.toLowerCase());
    }
}

