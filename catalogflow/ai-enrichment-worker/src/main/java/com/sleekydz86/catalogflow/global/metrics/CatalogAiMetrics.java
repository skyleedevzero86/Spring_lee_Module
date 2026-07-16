package com.sleekydz86.catalogflow.global.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class CatalogAiMetrics {

	private final Counter requested;
	private final Counter completed;
	private final Counter failed;
	private final Timer processingTimer;

	public CatalogAiMetrics(MeterRegistry meterRegistry) {
		this.requested = Counter.builder("catalog_ai_request_total")
				.description("AI 요청 횟수")
				.register(meterRegistry);
		this.completed = Counter.builder("catalog_ai_completed_total")
				.description("AI 성공 횟수")
				.register(meterRegistry);
		this.failed = Counter.builder("catalog_ai_failed_total")
				.description("AI 실패 횟수")
				.register(meterRegistry);
		this.processingTimer = Timer.builder("catalog_ai_processing_seconds")
				.description("AI 처리 시간")
				.register(meterRegistry);
	}

	public void incrementRequested() {
		requested.increment();
	}

	public void incrementCompleted() {
		completed.increment();
	}

	public void incrementFailed() {
		failed.increment();
	}

	public Timer.Sample startTimer() {
		return Timer.start();
	}

	public void record(Timer.Sample sample) {
		sample.stop(processingTimer);
	}
}
