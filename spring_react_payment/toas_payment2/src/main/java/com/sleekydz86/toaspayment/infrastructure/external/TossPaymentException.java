package com.sleekydz86.toaspayment.infrastructure.external;

public class TossPaymentException extends RuntimeException {
    private final int statusCode;

    public TossPaymentException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}


