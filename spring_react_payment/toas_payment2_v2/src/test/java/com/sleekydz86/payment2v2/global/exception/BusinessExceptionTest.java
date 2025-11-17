package com.sleekydz86.payment2v2.global.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BusinessException ?�스??)
class BusinessExceptionTest {

    @Test
    @DisplayName("ErrorCode만으�?BusinessException???�성?????�다")
    void ErrorCode만으�?BusinessException???�성?????�다() {

        // given
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;


        BusinessException exception = new BusinessException(errorCode);


        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }

    @Test
    @DisplayName("ErrorCode?� 메시지�?BusinessException???�성?????�다")
    void ErrorCode?�_메시지�?BusinessException???�성?????�다() {

        // given
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        String customMessage = "?�용???�의 메시지";


        BusinessException exception = new BusinessException(errorCode, customMessage);


        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
    }

    @Test
    @DisplayName("ErrorCode, 메시지, ?�인?�로 BusinessException???�성?????�다")
    void ErrorCode_메시지_?�인?�로_BusinessException???�성?????�다() {

        // given
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        String customMessage = "?�용???�의 메시지";
        Throwable cause = new IllegalArgumentException("?�인 ?�외");


        BusinessException exception = new BusinessException(errorCode, customMessage, cause);


        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(customMessage);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("ErrorCode???�보�??�인?????�다")
    void ErrorCode???�보�??�인?????�다() {

        // given
        ErrorCode errorCode = ErrorCode.PAYMENT_NOT_FOUND;


        BusinessException exception = new BusinessException(errorCode);


        assertThat(exception.getErrorCode().getCode()).isEqualTo("P004");
        assertThat(exception.getErrorCode().getMessage()).isEqualTo("결제 ?�보�?찾을 ???�습?�다.");
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
    }

