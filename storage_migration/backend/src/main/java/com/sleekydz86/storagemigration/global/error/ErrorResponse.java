package com.sleekydz86.storagemigration.global.error;

public record ErrorResponse(String code, String message, String detail) {

    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return new ErrorResponse(errorCode.code(), errorCode.message(), detail);
    }
}
