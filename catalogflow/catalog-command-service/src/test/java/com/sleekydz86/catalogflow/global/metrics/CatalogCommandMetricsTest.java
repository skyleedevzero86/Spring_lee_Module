package com.sleekydz86.catalogflow.global.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CatalogCommandMetricsTest {

	@Test
	@DisplayName("상품 등록 메트릭이 증가한다")
	void shouldIncrementProductCreated() {
		// given
		SimpleMeterRegistry registry = new SimpleMeterRegistry();
		CatalogCommandMetrics metrics = new CatalogCommandMetrics(registry);

		// when
		metrics.incrementProductCreated();
		metrics.incrementProductCreated();

		// then
		assertEquals(2.0, registry.get("catalog_product_created_total").counter().count());
	}
}
