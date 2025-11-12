package com.sleekydz86.toaspayment.application.usecase;

import com.sleekydz86.toaspayment.application.dto.PaymentLogResponse;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLog;
import com.sleekydz86.toaspayment.domain.paymentlog.PaymentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetOrderLogsUseCase {
    private final PaymentLogRepository paymentLogRepository;

    public List<PaymentLogResponse> execute(String orderId) {
        List<PaymentLog> logs = paymentLogRepository.findByOrderId(orderId);
        return logs.stream()
                .map(log -> new PaymentLogResponse(
                        log.getId(),
                        log.getOrderId(),
                        log.getMemberId(),
                        log.getLogType().name(),
                        log.getMessage(),
                        log.getDetails(),
                        log.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}


