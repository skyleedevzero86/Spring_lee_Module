package com.sleekydz86.payment2v2.domain.payment.application.port.in;

import com.sleekydz86.payment2v2.domain.payment.adapter.out.external.toss.dto.TossPaymentCallbackRequest;

public interface ProcessPaymentCallbackUseCase {
    void processCallback(TossPaymentCallbackRequest callbackRequest);
}

