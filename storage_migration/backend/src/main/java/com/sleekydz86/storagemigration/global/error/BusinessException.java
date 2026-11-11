package com.sleekydz86.storagemigration.global.error;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        this(errorCode, detail, null);
    }

    public BusinessException(ErrorCode errorCode, String detail, Throwable cause) {
        super(detail == null ? errorCode.message() : errorCode.message() + " " + detail, cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public String detail() {
        return detail;
    }
}
