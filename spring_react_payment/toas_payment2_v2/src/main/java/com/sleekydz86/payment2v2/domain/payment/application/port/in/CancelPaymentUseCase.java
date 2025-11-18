package com.sleekydz86.payment2v2.domain.payment.application.port.in;

import com.sleekydz86.payment2v2.domain.payment.application.dto.CancelPaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.CancelPaymentResponse;

public interface CancelPaymentUseCase {
    CancelPaymentResponse cancelPayment(CancelPaymentCommand command);
}

