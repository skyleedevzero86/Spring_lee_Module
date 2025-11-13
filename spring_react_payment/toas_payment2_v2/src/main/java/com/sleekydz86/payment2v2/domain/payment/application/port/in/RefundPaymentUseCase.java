package com.sleekydz86.payment2v2.domain.payment.application.port.in;

import com.sleekydz86.payment2v2.domain.payment.application.dto.RefundPaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.RefundPaymentResponse;

public interface RefundPaymentUseCase {
    RefundPaymentResponse refundPayment(RefundPaymentCommand command);
}

