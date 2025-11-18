package com.sleekydz86.payment2v2.domain.payment.application.port.in;

import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentDetailResponse;

public interface GetPaymentDetailUseCase {
    PaymentDetailResponse getPaymentDetail(Long paymentId, Long userId, String userRole);
}
