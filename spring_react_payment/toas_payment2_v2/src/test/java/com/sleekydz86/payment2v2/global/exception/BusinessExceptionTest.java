package com.sleekydz86.payment2v2.global.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BusinessException 테스트")
class BusinessExceptionTest {

    @Test
    @DisplayName("ErrorCode만으로 BusinessException을 생성할 수 있다")
    void ErrorCode만으로_BusinessException을_생성할_수_있다() {
        // given
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        // when
        BusinessException exception = new BusinessException(errorCode);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("ErrorCode와 메시지로 BusinessException을 생성할 수 있다")
    void ErrorCode와_메시지로_BusinessException을_생성할_수_있다() {
        // given
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        String customMessage = "사용자 정의 메시지";

        // when
        BusinessException exception = new BusinessException(errorCode, customMessage);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
    }

    @Test
    @DisplayName("ErrorCode, 메시지, 원인으로 BusinessException을 생성할 수 있다")
    void ErrorCode_메시지_원인으로_BusinessException을_생성할_수_있다() {
        // given
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        String customMessage = "사용자 정의 메시지";
        Throwable cause = new IllegalArgumentException("원인 예외");

        // when
        BusinessException exception = new BusinessException(errorCode, customMessage, cause);

        // then
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("ErrorCode의 정보를 확인할 수 있다")
    void ErrorCode의_정보를_확인할_수_있다() {
        // given
        ErrorCode errorCode = ErrorCode.PAYMENT_NOT_FOUND;

        // when
        BusinessException exception = new BusinessException(errorCode);

        // then
        assertThat(exception.getErrorCode().getCode()).isEqualTo("P004");
        assertThat(exception.getErrorCode().getMessage()).isEqualTo("결제 정보를 찾을 수 없습니다.");
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
    }
}

