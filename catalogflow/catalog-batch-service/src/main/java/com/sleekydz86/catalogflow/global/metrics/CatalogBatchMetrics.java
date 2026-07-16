package com.sleekydz86.catalogflow.global.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CatalogBatchMetrics {

	private final Counter jobStarted;
	private final Counter jobCompleted;
	private final Counter jobFailed;
	private final Counter itemsProcessed;

	public CatalogBatchMetrics(MeterRegistry meterRegistry) {
		this.jobStarted = Counter.builder("catalog_batch_job_started_total")
				.description("배치 Job 시작 횟수")
				.register(meterRegistry);
		this.jobCompleted = Counter.builder("catalog_batch_job_completed_total")
				.description("배치 Job 성공 횟수")
				.register(meterRegistry);
		this.jobFailed = Counter.builder("catalog_batch_job_failed_total")
				.description("배치 Job 실패 횟수")
				.register(meterRegistry);
		this.itemsProcessed = Counter.builder("catalog_batch_items_processed_total")
				.description("배치 처리 건수")
				.register(meterRegistry);
	}

	public void incrementJobStarted() {
		jobStarted.increment();
	}

	public void incrementJobCompleted() {
		jobCompleted.increment();
	}

	public void incrementJobFailed() {
		jobFailed.increment();
	}

	public void incrementItemsProcessed(double amount) {
		itemsProcessed.increment(amount);
	}
}
