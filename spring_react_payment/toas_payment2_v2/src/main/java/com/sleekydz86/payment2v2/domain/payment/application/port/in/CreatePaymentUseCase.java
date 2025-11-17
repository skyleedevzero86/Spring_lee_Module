package com.sleekydz86.payment2v2.domain.payment.application.port.in;

import com.sleekydz86.payment2v2.domain.payment.application.dto.CreatePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentResponse;

public interface CreatePaymentUseCase {
    PaymentResponse createPayment(CreatePaymentCommand command);
}


