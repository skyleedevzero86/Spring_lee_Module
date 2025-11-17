package com.sleekydz86.payment2v2.global.exception;

public class TossPaymentClientException extends RuntimeException {
    public TossPaymentClientException(String message) {
        super(message);
    }

    public TossPaymentClientException(String message, Throwable cause) {
        super(message, cause);
    }
}

