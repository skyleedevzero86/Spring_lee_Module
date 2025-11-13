package com.sleekydz86.payment2v2.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final String code;
    private final String message;
    private final String detail;

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .detail(detail)
                .build();
    }
}

