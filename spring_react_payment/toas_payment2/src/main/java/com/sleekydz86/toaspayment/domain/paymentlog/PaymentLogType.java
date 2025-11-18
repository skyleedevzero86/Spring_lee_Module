package com.sleekydz86.toaspayment.domain.paymentlog;

public enum PaymentLogType {
    PAYMENT_INIT,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PAYMENT_CANCELLED,
    REFUND_REQUESTED,
    REFUND_SUCCESS,
    REFUND_FAILED,
    PAYMENT_ERROR
}
