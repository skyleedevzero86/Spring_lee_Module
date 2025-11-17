package com.sleekydz86.payment2v2.domain.payment.application.port.in;

import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentCallbackCommand;

public interface ProcessPaymentCallbackUseCase {
    void processCallback(PaymentCallbackCommand callbackCommand);
}

