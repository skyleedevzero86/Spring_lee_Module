package com.sleekydz86.catalogflow.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.out.ProductViewStore;
import com.sleekydz86.catalogflow.application.query.ProductQueryCriteria;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductViewProjectionServiceTest {

	private InMemoryProductViewStore productViewStore;
	private ProductViewProjectionService projectionService;

	@BeforeEach
	void setUp() {
		productViewStore = new InMemoryProductViewStore();
		projectionService = new ProductViewProjectionService(productViewStore);
	}

	@Test
	void shouldCreateProductViewFromProductCreatedEvent() {
		UUID productId = UUID.randomUUID();
		UUID categoryId = UUID.randomUUID();
		UUID supplierId = UUID.randomUUID();
		Instant now = Instant.parse("2026-07-16T09:00:00Z");
		String payload = "{"
				+ "\"eventId\":\"" + UUID.randomUUID() + "\","
				+ "\"eventType\":\"" + CatalogEventTypes.PRODUCT_CREATED + "\","
				+ "\"aggregateId\":\"" + productId + "\","
				+ "\"aggregateVersion\":0,"
				+ "\"occurredAt\":\"" + now + "\","
				+ "\"correlationId\":\"corr-1\","
				+ "\"causationId\":\"\","
				+ "\"schemaVersion\":1,"
				+ "\"name\":\"무선 키보드\","
				+ "\"description\":\"저소음 키보드\","
				+ "\"priceAmount\":59000,"
				+ "\"priceCurrency\":\"KRW\","
				+ "\"categoryId\":\"" + categoryId + "\","
				+ "\"supplierId\":\"" + supplierId + "\","
				+ "\"supplierName\":\"기본 공급사\","
				+ "\"status\":\"DRAFT\","
				+ "\"createdAt\":\"" + now + "\","
				+ "\"updatedAt\":\"" + now + "\""
				+ "}";

		projectionService.project(new IntegrationEventEnvelope(
				UUID.randomUUID(),
				CatalogEventTypes.PRODUCT_CREATED,
				productId,
				0L,
				now,
				"corr-1",
				"",
				1,
				payload));

		ProductView view = productViewStore.findByProductId(productId).orElseThrow();
		assertEquals("무선 키보드", view.getName());
		assertEquals(new BigDecimal("59000"), view.getPrice());
		assertEquals("KRW", view.getCurrency());
		assertEquals("DRAFT", view.getStatus());
		assertEquals("기본 공급사", view.getSupplierName());
		assertEquals(0L, view.getVersion());
	}

	@Test
	void shouldIgnoreStaleEventVersion() {
		UUID productId = UUID.randomUUID();
		Instant now = Instant.parse("2026-07-16T09:00:00Z");
		ProductView existing = ProductView.create(productId);
		existing.setName("최신 상품");
		existing.setVersion(3L);
		existing.setStatus("DRAFT");
		productViewStore.save(existing);

		String payload = "{"
				+ "\"eventId\":\"" + UUID.randomUUID() + "\","
				+ "\"eventType\":\"" + CatalogEventTypes.PRODUCT_UPDATED + "\","
				+ "\"aggregateId\":\"" + productId + "\","
				+ "\"aggregateVersion\":2,"
				+ "\"occurredAt\":\"" + now + "\","
				+ "\"correlationId\":\"corr-1\","
				+ "\"causationId\":\"\","
				+ "\"schemaVersion\":1,"
				+ "\"name\":\"오래된 상품\","
				+ "\"description\":\"설명\","
				+ "\"categoryId\":\"" + UUID.randomUUID() + "\","
				+ "\"supplierId\":\"" + UUID.randomUUID() + "\","
				+ "\"supplierName\":\"공급사\","
				+ "\"status\":\"DRAFT\","
				+ "\"updatedAt\":\"" + now + "\""
				+ "}";

		projectionService.project(new IntegrationEventEnvelope(
				UUID.randomUUID(),
				CatalogEventTypes.PRODUCT_UPDATED,
				productId,
				2L,
				now,
				"corr-1",
				"",
				1,
				payload));

		assertEquals("최신 상품", productViewStore.findByProductId(productId).orElseThrow().getName());
		assertEquals(3L, productViewStore.findByProductId(productId).orElseThrow().getVersion());
	}

	@Test
	void shouldApplyProductPriceChangedEvent() {
		UUID productId = UUID.randomUUID();
		Instant now = Instant.parse("2026-07-16T09:00:00Z");
		ProductView existing = ProductView.create(productId);
		existing.setName("상품");
		existing.setPrice(new BigDecimal("10000"));
		existing.setCurrency("KRW");
		existing.setVersion(1L);
		existing.setStatus("DRAFT");
		productViewStore.save(existing);

		String payload = "{"
				+ "\"eventId\":\"" + UUID.randomUUID() + "\","
				+ "\"eventType\":\"" + CatalogEventTypes.PRODUCT_PRICE_CHANGED + "\","
				+ "\"aggregateId\":\"" + productId + "\","
				+ "\"aggregateVersion\":2,"
				+ "\"occurredAt\":\"" + now + "\","
				+ "\"correlationId\":\"corr-1\","
				+ "\"causationId\":\"\","
				+ "\"schemaVersion\":1,"
				+ "\"priceAmount\":12000,"
				+ "\"priceCurrency\":\"KRW\","
				+ "\"updatedAt\":\"" + now + "\""
				+ "}";

		projectionService.project(new IntegrationEventEnvelope(
				UUID.randomUUID(),
				CatalogEventTypes.PRODUCT_PRICE_CHANGED,
				productId,
				2L,
				now,
				"corr-1",
				"",
				1,
				payload));

		ProductView view = productViewStore.findByProductId(productId).orElseThrow();
		assertEquals(new BigDecimal("12000"), view.getPrice());
		assertEquals(2L, view.getVersion());
	}

	@Test
	void shouldApplyProductPublishedEvent() {
		UUID productId = UUID.randomUUID();
		Instant now = Instant.parse("2026-07-16T09:00:00Z");
		ProductView existing = ProductView.create(productId);
		existing.setName("상품");
		existing.setVersion(4L);
		existing.setStatus("READY");
		productViewStore.save(existing);

		String payload = "{"
				+ "\"eventId\":\"" + UUID.randomUUID() + "\","
				+ "\"eventType\":\"" + CatalogEventTypes.PRODUCT_PUBLISHED + "\","
				+ "\"aggregateId\":\"" + productId + "\","
				+ "\"aggregateVersion\":5,"
				+ "\"occurredAt\":\"" + now + "\","
				+ "\"correlationId\":\"corr-1\","
				+ "\"causationId\":\"\","
				+ "\"schemaVersion\":1,"
				+ "\"status\":\"PUBLISHED\","
				+ "\"publishedAt\":\"" + now + "\","
				+ "\"updatedAt\":\"" + now + "\""
				+ "}";

		projectionService.project(new IntegrationEventEnvelope(
				UUID.randomUUID(),
				CatalogEventTypes.PRODUCT_PUBLISHED,
				productId,
				5L,
				now,
				"corr-1",
				"",
				1,
				payload));

		ProductView view = productViewStore.findByProductId(productId).orElseThrow();
		assertEquals("PUBLISHED", view.getStatus());
		assertEquals(now, view.getPublishedAt());
		assertEquals(5L, view.getVersion());
	}

	private static final class InMemoryProductViewStore implements ProductViewStore {

		private ProductView stored;

		@Override
		public Optional<ProductView> findByProductId(UUID productId) {
			if (stored == null || !stored.getProductId().equals(productId)) {
				return Optional.empty();
			}
			return Optional.of(copy(stored));
		}

		@Override
		public void save(ProductView productView) {
			stored = copy(productView);
		}

		@Override
		public List<ProductView> findByCriteria(ProductQueryCriteria criteria, int fetchSize) {
			if (stored == null) {
				return List.of();
			}
			return List.of(copy(stored));
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
