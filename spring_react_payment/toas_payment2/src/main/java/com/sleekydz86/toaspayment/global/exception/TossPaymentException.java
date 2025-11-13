package com.sleekydz86.toaspayment.global.exception;

import org.springframework.http.HttpStatus;

public class TossPaymentException extends RuntimeException {
    private final HttpStatus status;

    public TossPaymentException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
