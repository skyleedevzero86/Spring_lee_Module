package com.sleekydz86.payment2v2.domain.payment.application.port.in;

import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentHistoryResponse;

import java.util.List;

public interface GetPaymentHistoryUseCase {
    List<PaymentHistoryResponse> getPaymentHistory(Long userId, String userRole);
}
