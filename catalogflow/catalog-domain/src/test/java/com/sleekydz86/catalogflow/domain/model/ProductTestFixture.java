package com.sleekydz86.catalogflow.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

final class ProductTestFixture {

	static final Instant NOW = Instant.parse("2026-07-16T05:00:00Z");
	static final String CORRELATION_ID = "corr-test-001";

	private ProductTestFixture() {
	}

	static ProductId productId() {
		return ProductId.of("11111111-1111-1111-1111-111111111111");
	}

	static CategoryId categoryId() {
		return CategoryId.of("22222222-2222-2222-2222-222222222222");
	}

	static SupplierId supplierId() {
		return SupplierId.of("33333333-3333-3333-3333-333333333333");
	}

	static ProductName productName() {
		return new ProductName("Wireless Mouse");
	}

	static ProductDescription productDescription() {
		return new ProductDescription("Ergonomic wireless mouse");
	}

	static Money price() {
		return Money.of(29_900, "KRW");
	}

	static ImageReference imageReference() {
		return ImageReference.create(
				"products/" + UUID.randomUUID() + "/mouse.png",
				"image/png",
				1024L,
				false,
				NOW);
	}

	static Product draftProduct() {
		Product product = Product.create(
				productId(),
				productName(),
				productDescription(),
				price(),
				categoryId(),
				supplierId(),
				NOW,
				CORRELATION_ID);
		product.pullDomainEvents();
		return product;
	}

	static Product readyProductWithImage() {
		Product product = draftProduct();
		product.uploadImage(0L, imageReference(), NOW, CORRELATION_ID);
		product.pullDomainEvents();
		product.requestAiEnrichment(1L, NOW, CORRELATION_ID);
		product.pullDomainEvents();
		product.completeAiEnrichment(
				2L,
				"stub",
				Set.of(new ProductKeyword("mouse"), new ProductKeyword("wireless")),
				Set.of(new ProductTag("electronics")),
				NOW,
				CORRELATION_ID);
		product.pullDomainEvents();
		product.approveAiEnrichment(3L, NOW, CORRELATION_ID);
		product.pullDomainEvents();
		return product;
	}
}
