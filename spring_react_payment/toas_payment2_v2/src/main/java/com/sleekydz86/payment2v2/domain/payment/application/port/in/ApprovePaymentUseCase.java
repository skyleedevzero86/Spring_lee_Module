package com.sleekydz86.payment2v2.domain.payment.application.port.in;

import com.sleekydz86.payment2v2.domain.payment.application.dto.ApprovePaymentCommand;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentApprovalResponse;

public interface ApprovePaymentUseCase {
    PaymentApprovalResponse approvePayment(ApprovePaymentCommand command);
}
