package com.sleekydz86.payment2v2.domain.payment.application.port.in;

import com.sleekydz86.payment2v2.domain.payment.application.dto.GetPaymentStatusCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentStatusResponse;

public interface GetPaymentStatusUseCase {
    PaymentStatusResponse getPaymentStatus(GetPaymentStatusCommand command);
}


