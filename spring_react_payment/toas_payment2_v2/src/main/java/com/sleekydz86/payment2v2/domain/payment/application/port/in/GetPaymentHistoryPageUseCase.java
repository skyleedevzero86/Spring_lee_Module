package com.sleekydz86.payment2v2.domain.payment.application.port.in;

import com.sleekydz86.payment2v2.global.dto.PageResponse;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentHistoryResponse;
import org.springframework.data.domain.Pageable;

public interface GetPaymentHistoryPageUseCase {
    PageResponse<PaymentHistoryResponse> getPaymentHistory(Long userId, String userRole, Pageable pageable);
}
