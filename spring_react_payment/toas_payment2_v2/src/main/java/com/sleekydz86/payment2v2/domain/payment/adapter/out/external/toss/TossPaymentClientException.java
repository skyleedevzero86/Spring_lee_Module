package com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss;

public class TossPaymentClientException extends RuntimeException {
    public TossPaymentClientException(String message) {
        super(message);
    }

    public TossPaymentClientException(String message, Throwable cause) {
        super(message, cause);
    }
}

