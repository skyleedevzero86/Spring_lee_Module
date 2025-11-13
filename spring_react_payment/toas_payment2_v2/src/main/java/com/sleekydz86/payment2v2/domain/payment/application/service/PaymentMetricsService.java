package com.sleekydz86.payment2v2.domain.payment.application.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PaymentMetricsService {

    private final MeterRegistry meterRegistry;

    public void recordPaymentCreated() {
        Counter.builder("payment.created")
                .description("결제 생성 횟수")
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentCompleted() {
        Counter.builder("payment.completed")
                .description("결제 완료 횟수")
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentRefunded() {
        Counter.builder("payment.refunded")
                .description("결제 환불 횟수")
                .register(meterRegistry)
                .increment();
    }

    public void recordPaymentApiCall(String apiName, long duration, TimeUnit unit) {
        Timer.builder("payment.api.call")
                .description("결제 API 호출 시간")
                .tag("api", apiName)
                .register(meterRegistry)
                .record(duration, unit);
    }

    public void recordPaymentApiError(String apiName) {
        Counter.builder("payment.api.error")
                .description("결제 API 오류 횟수")
                .tag("api", apiName)
                .register(meterRegistry)
                .increment();
    }
}

