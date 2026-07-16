package com.sleekydz86.catalogflow.global.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class CatalogQueryMetrics {

	private final Counter cacheHit;
	private final Counter cacheMiss;
	private final Counter productQueried;

	public CatalogQueryMetrics(MeterRegistry meterRegistry) {
		this.cacheHit = Counter.builder("catalog_cache_hit_total")
				.description("캐시 히트 횟수")
				.register(meterRegistry);
		this.cacheMiss = Counter.builder("catalog_cache_miss_total")
				.description("캐시 미스 횟수")
				.register(meterRegistry);
		this.productQueried = Counter.builder("catalog_product_queried_total")
				.description("상품 조회 횟수")
				.register(meterRegistry);
	}

	public void incrementCacheHit() {
		cacheHit.increment();
	}

	public void incrementCacheMiss() {
		cacheMiss.increment();
	}

	public void incrementProductQueried() {
		productQueried.increment();
	}
}
