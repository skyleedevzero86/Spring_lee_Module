package com.sleekydz86.payment2v2.global.util;

import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.TossPaymentClientException;
import com.sleekydz86.payment2v2.global.exception.BusinessException;
import com.sleekydz86.payment2v2.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TossPaymentExceptionHandler {
    
    private TossPaymentExceptionHandler() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    public static <T> T handleTossApiCall(String operation, String orderNo, TossApiCallable<T> apiCall) {
        try {
            return apiCall.call();
        } catch (TossPaymentClientException e) {
            log.error("토스페이먼츠 {} API 호출 중 오류 발생: orderNo={}", operation, orderNo, e);
            throw new BusinessException(ErrorCode.TOSS_PAYMENT_API_ERROR, e.getMessage(), e);
        }
    }

    public static <T> T validateTossResponse(String operation, String orderNo, T response, TossResponseValidator<T> validator) {
        if (!validator.isSuccess(response)) {
            String errorMessage = validator.getErrorMessage(response);
            log.error("토스페이먼츠 {} 실패: orderNo={}, message={}", operation, orderNo, errorMessage);
            throw new BusinessException(validator.getErrorCode(), 
                    errorMessage != null ? errorMessage : String.format("%s에 실패했습니다.", operation));
        }
        return response;
    }

    @FunctionalInterface
    public interface TossApiCallable<T> {
        T call() throws TossPaymentClientException;
    }

    @FunctionalInterface
    public interface TossResponseValidator<T> {
        boolean isSuccess(T response);
        String getErrorMessage(T response);
        ErrorCode getErrorCode();
    }
}

