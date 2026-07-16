package com.sleekydz86.catalogflow.global.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CatalogCommandMetrics {

	private final Counter productCreated;
	private final Counter productUpdated;
	private final Counter aiEnrichmentRequested;

	public CatalogCommandMetrics(MeterRegistry meterRegistry) {
		this.productCreated = Counter.builder("catalog_product_created_total")
				.description("상품 등록 횟수")
				.register(meterRegistry);
		this.productUpdated = Counter.builder("catalog_product_updated_total")
				.description("상품 수정 횟수")
				.register(meterRegistry);
		this.aiEnrichmentRequested = Counter.builder("catalog_ai_enrichment_requested_total")
				.description("AI 가공 요청 횟수")
				.register(meterRegistry);
	}

	public void incrementProductCreated() {
		productCreated.increment();
	}

	public void incrementProductUpdated() {
		productUpdated.increment();
	}

	public void incrementAiEnrichmentRequested() {
		aiEnrichmentRequested.increment();
	}
}
