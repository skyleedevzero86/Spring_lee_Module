package com.sleekydz86.catalogflow.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.out.ProductCachePort;
import com.sleekydz86.catalogflow.application.port.out.ProductViewStore;
import com.sleekydz86.catalogflow.application.query.ProductPageResult;
import com.sleekydz86.catalogflow.application.query.ProductQueryCriteria;
import com.sleekydz86.catalogflow.global.exception.ProductNotFoundException;
import com.sleekydz86.catalogflow.global.metrics.CatalogQueryMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductQueryServiceCacheTest {

	private CountingStore store;
	private InMemoryCache cache;
	private ProductQueryService productQueryService;

	@BeforeEach
	void setUp() {
		store = new CountingStore();
		cache = new InMemoryCache();
		productQueryService = new ProductQueryService(
				store,
				cache,
				new CatalogQueryMetrics(new SimpleMeterRegistry()));
	}

	@Test
	@DisplayName("상품 상세 조회는 Cache Aside로 동작한다")
	void shouldUseCacheAsideForProductDetail() {
		// given
		UUID productId = UUID.randomUUID();
		ProductView view = sample(productId, "캐시된 상품");
		store.save(view);

		// when
		ProductView first = productQueryService.getById(productId);
		ProductView second = productQueryService.getById(productId);

		// then
		assertEquals("캐시된 상품", first.getName());
		assertEquals("캐시된 상품", second.getName());
		assertEquals(1, store.findCount.get());
		assertTrue(cache.products.containsKey(productId));
	}

	@Test
	void shouldCacheMissAndThrowNotFound() {
		UUID productId = UUID.randomUUID();
		assertThrows(ProductNotFoundException.class, () -> productQueryService.getById(productId));
		assertTrue(cache.missMarkers.containsKey(productId));
		assertThrows(ProductNotFoundException.class, () -> productQueryService.getById(productId));
		assertEquals(1, store.findCount.get());
	}

	@Test
	void shouldCachePopularProducts() {
		UUID productId = UUID.randomUUID();
		store.save(sample(productId, "인기 상품"));

		ProductPageResult first = productQueryService.listPopular(ProductQueryCriteria.popular(10));
		ProductPageResult second = productQueryService.listPopular(ProductQueryCriteria.popular(10));

		assertEquals(1, first.items().size());
		assertSame(first, second);
		assertEquals(1, store.criteriaCount.get());
	}

	private ProductView sample(UUID productId, String name) {
		ProductView view = ProductView.create(productId);
		view.setName(name);
		view.setSummary(name);
		view.setDescription(name);
		view.setPrice(new BigDecimal("10000"));
		view.setCurrency("KRW");
		view.setStatus("PUBLISHED");
		view.setCategoryId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
		view.setSupplierId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
		view.setPublishedAt(Instant.parse("2026-07-16T12:00:00Z"));
		view.setCreatedAt(Instant.parse("2026-07-16T12:00:00Z"));
		view.setUpdatedAt(Instant.parse("2026-07-16T12:00:00Z"));
		view.setVersion(1L);
		return view;
	}

	private static final class CountingStore implements ProductViewStore {

		private final Map<UUID, ProductView> values = new HashMap<>();
		private final AtomicInteger findCount = new AtomicInteger();
		private final AtomicInteger criteriaCount = new AtomicInteger();

		@Override
		public Optional<ProductView> findByProductId(UUID productId) {
			findCount.incrementAndGet();
			return Optional.ofNullable(values.get(productId));
		}

		@Override
		public void save(ProductView productView) {
			values.put(productView.getProductId(), productView);
		}

		@Override
		public List<ProductView> findByCriteria(ProductQueryCriteria criteria, int fetchSize) {
			criteriaCount.incrementAndGet();
			return values.values().stream().limit(fetchSize).toList();
		}
	}

	private static final class InMemoryCache implements ProductCachePort {

		private final Map<UUID, ProductView> products = new HashMap<>();
		private final Map<UUID, Boolean> missMarkers = new HashMap<>();
		private final Map<Integer, ProductPageResult> popular = new HashMap<>();
		private final Map<String, ProductPageResult> categoryPages = new HashMap<>();

		@Override
		public Optional<ProductView> getProduct(UUID productId) {
			return Optional.ofNullable(products.get(productId));
		}

		@Override
		public void putProduct(ProductView productView) {
			products.put(productView.getProductId(), productView);
			missMarkers.remove(productView.getProductId());
		}

		@Override
		public void putProductMiss(UUID productId) {
			missMarkers.put(productId, true);
		}

		@Override
		public boolean isProductMiss(UUID productId) {
			return missMarkers.containsKey(productId);
		}

		@Override
		public Optional<ProductPageResult> getCategoryPage(UUID categoryId, String status, String cursor, int size) {
			return Optional.ofNullable(categoryPages.get(categoryId + ":" + status + ":" + cursor + ":" + size));
		}

		@Override
		public void putCategoryPage(
				UUID categoryId,
				String status,
				String cursor,
				int size,
				ProductPageResult pageResult) {
			categoryPages.put(categoryId + ":" + status + ":" + cursor + ":" + size, pageResult);
		}

		@Override
		public Optional<ProductPageResult> getPopular(int size) {
			return Optional.ofNullable(popular.get(size));
		}

		@Override
		public void putPopular(int size, ProductPageResult pageResult) {
			popular.put(size, pageResult);
		}

		@Override
		public void evictProductRelated(UUID productId, UUID categoryId) {
			products.remove(productId);
			missMarkers.remove(productId);
			popular.clear();
			categoryPages.clear();
		}
	}
}
