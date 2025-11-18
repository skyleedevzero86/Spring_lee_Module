package com.sleekydz86.payment2v2.global.exception;

public class DomainException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String domainMessage;

    public DomainException(ErrorCode errorCode, String domainMessage) {
        super(domainMessage);
        this.errorCode = errorCode;
        this.domainMessage = domainMessage;
    }

    public DomainException(ErrorCode errorCode, String domainMessage, Throwable cause) {
        super(domainMessage, cause);
        this.errorCode = errorCode;
        this.domainMessage = domainMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDomainMessage() {
        return domainMessage;
    }
}
