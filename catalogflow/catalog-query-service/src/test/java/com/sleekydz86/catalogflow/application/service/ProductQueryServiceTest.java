package com.sleekydz86.catalogflow.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.out.ProductViewStore;
import com.sleekydz86.catalogflow.application.query.ProductPageResult;
import com.sleekydz86.catalogflow.application.query.ProductQueryCriteria;
import com.sleekydz86.catalogflow.global.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductQueryServiceTest {

	private InMemoryProductViewStore store;
	private ProductQueryService productQueryService;

	@BeforeEach
	void setUp() {
		store = new InMemoryProductViewStore();
		productQueryService = new ProductQueryService(store);
	}

	@Test
	void shouldGetProductById() {
		UUID productId = UUID.randomUUID();
		store.save(publishedProduct(productId, "무선 키보드", Instant.parse("2026-07-16T10:00:00Z")));

		ProductView view = productQueryService.getById(productId);

		assertEquals("무선 키보드", view.getName());
	}

	@Test
	void shouldThrowWhenProductNotFound() {
		UUID productId = UUID.randomUUID();
		assertThrows(ProductNotFoundException.class, () -> productQueryService.getById(productId));
	}

	@Test
	void shouldPaginateProductsWithKeysetCursor() {
		UUID firstId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID secondId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		UUID thirdId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		store.save(publishedProduct(firstId, "상품1", Instant.parse("2026-07-16T12:00:00Z")));
		store.save(publishedProduct(secondId, "상품2", Instant.parse("2026-07-16T11:00:00Z")));
		store.save(publishedProduct(thirdId, "상품3", Instant.parse("2026-07-16T10:00:00Z")));

		ProductPageResult firstPage = productQueryService.list(
				ProductQueryCriteria.list("PUBLISHED", null, null, 2));

		assertEquals(2, firstPage.items().size());
		assertTrue(firstPage.hasNext());
		assertEquals("상품1", firstPage.items().get(0).getName());
		assertEquals("상품2", firstPage.items().get(1).getName());

		String[] cursorParts = firstPage.nextCursor().split("\\|", 2);
		ProductPageResult secondPage = productQueryService.list(
				ProductQueryCriteria.list(
						"PUBLISHED",
						Instant.parse(cursorParts[0]),
						cursorParts[1],
						2));

		assertEquals(1, secondPage.items().size());
		assertFalse(secondPage.hasNext());
		assertEquals("상품3", secondPage.items().get(0).getName());
	}

	@Test
	void shouldSearchByName() {
		store.save(publishedProduct(UUID.randomUUID(), "무선 키보드", Instant.parse("2026-07-16T10:00:00Z")));
		store.save(publishedProduct(UUID.randomUUID(), "유선 마우스", Instant.parse("2026-07-16T09:00:00Z")));

		ProductPageResult result = productQueryService.search(
				ProductQueryCriteria.search("키보드", null, "PUBLISHED", null, null, null, null, 20));

		assertEquals(1, result.items().size());
		assertEquals("무선 키보드", result.items().get(0).getName());
	}

	private ProductView publishedProduct(UUID productId, String name, Instant publishedAt) {
		ProductView view = ProductView.create(productId);
		view.setName(name);
		view.setSummary(name);
		view.setDescription(name);
		view.setPrice(new BigDecimal("10000"));
		view.setCurrency("KRW");
		view.setStatus("PUBLISHED");
		view.setCategoryId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
		view.setSupplierId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
		view.setSupplierName("기본 공급사");
		view.setPublishedAt(publishedAt);
		view.setCreatedAt(publishedAt);
		view.setUpdatedAt(publishedAt);
		view.setVersion(1L);
		return view;
	}

	private static final class InMemoryProductViewStore implements ProductViewStore {

		private final List<ProductView> values = new ArrayList<>();

		@Override
		public Optional<ProductView> findByProductId(UUID productId) {
			return values.stream()
					.filter(view -> view.getProductId().equals(productId))
					.map(this::copy)
					.findFirst();
		}

		@Override
		public void save(ProductView productView) {
			values.removeIf(view -> view.getProductId().equals(productView.getProductId()));
			values.add(copy(productView));
		}

		@Override
		public List<ProductView> findByCriteria(ProductQueryCriteria criteria, int fetchSize) {
			return values.stream()
					.filter(view -> matches(view, criteria))
					.sorted(Comparator
							.comparing(ProductView::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder()))
							.thenComparing(view -> view.getProductId().toString(), Comparator.reverseOrder()))
					.filter(view -> afterCursor(view, criteria))
					.limit(fetchSize)
					.map(this::copy)
					.toList();
		}

		private boolean matches(ProductView view, ProductQueryCriteria criteria) {
			if (criteria.status() != null && !criteria.status().isBlank()
					&& !criteria.status().equals(view.getStatus())) {
				return false;
			}
			if (criteria.categoryId() != null && !criteria.categoryId().equals(view.getCategoryId())) {
				return false;
			}
			if (criteria.name() != null && !criteria.name().isBlank()
					&& (view.getName() == null || !view.getName().contains(criteria.name()))) {
				return false;
			}
			if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
				boolean matched = (view.getName() != null && view.getName().contains(criteria.keyword()))
						|| (view.getSummary() != null && view.getSummary().contains(criteria.keyword()))
						|| view.getKeywords().stream().anyMatch(keyword -> keyword.contains(criteria.keyword()));
				if (!matched) {
					return false;
				}
			}
			if (criteria.minPrice() != null && view.getPrice().compareTo(criteria.minPrice()) < 0) {
				return false;
			}
			if (criteria.maxPrice() != null && view.getPrice().compareTo(criteria.maxPrice()) > 0) {
				return false;
			}
			return true;
		}

		private boolean afterCursor(ProductView view, ProductQueryCriteria criteria) {
			if (criteria.cursorPublishedAt() == null && (criteria.cursorProductId() == null
					|| criteria.cursorProductId().isBlank())) {
				return true;
			}
			Instant publishedAt = view.getPublishedAt();
			String productId = view.getProductId().toString();
			if (criteria.cursorPublishedAt() == null) {
				return productId.compareTo(criteria.cursorProductId()) < 0;
			}
			if (publishedAt.isBefore(criteria.cursorPublishedAt())) {
				return true;
			}
			return publishedAt.equals(criteria.cursorPublishedAt())
					&& productId.compareTo(criteria.cursorProductId()) < 0;
		}

		private ProductView copy(ProductView source) {
			ProductView copy = ProductView.create(source.getProductId());
			copy.setName(source.getName());
			copy.setSummary(source.getSummary());
			copy.setDescription(source.getDescription());
			copy.setPrice(source.getPrice());
			copy.setCurrency(source.getCurrency());
			copy.setStatus(source.getStatus());
			copy.setCategoryId(source.getCategoryId());
			copy.setSupplierId(source.getSupplierId());
			copy.setSupplierName(source.getSupplierName());
			copy.setImageUrls(source.getImageUrls());
			copy.setKeywords(source.getKeywords());
			copy.setTags(source.getTags());
			copy.setAiGenerated(source.isAiGenerated());
			copy.setAiModel(source.getAiModel());
			copy.setPublishedAt(source.getPublishedAt());
			copy.setCreatedAt(source.getCreatedAt());
			copy.setUpdatedAt(source.getUpdatedAt());
			copy.setVersion(source.getVersion());
			return copy;
		}
	}
}
