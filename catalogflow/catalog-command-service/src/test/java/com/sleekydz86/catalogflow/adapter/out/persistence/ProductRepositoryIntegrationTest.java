package com.sleekydz86.catalogflow.adapter.out.persistence;

import com.sleekydz86.catalogflow.application.port.out.ProductRepository;
import com.sleekydz86.catalogflow.domain.model.CategoryId;
import com.sleekydz86.catalogflow.domain.model.ImageReference;
import com.sleekydz86.catalogflow.domain.model.Money;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductDescription;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.domain.model.ProductKeyword;
import com.sleekydz86.catalogflow.domain.model.ProductName;
import com.sleekydz86.catalogflow.domain.model.ProductStatus;
import com.sleekydz86.catalogflow.domain.model.ProductTag;
import com.sleekydz86.catalogflow.domain.model.SupplierId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class ProductRepositoryIntegrationTest {

	private static final Instant NOW = Instant.parse("2026-07-16T06:00:00Z");
	private static final ProductId PRODUCT_ID = ProductId.of("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private static final CategoryId CATEGORY_ID = CategoryId.of("22222222-2222-2222-2222-222222222222");
	private static final SupplierId SUPPLIER_ID = SupplierId.of("33333333-3333-3333-3333-333333333333");

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@Autowired
	private ProductRepository productRepository;

	@Test
	void shouldSaveAndLoadProduct() {
		Product product = Product.create(
				PRODUCT_ID,
				new ProductName("기계식 키보드"),
				new ProductDescription("청축 스위치 탑재"),
				Money.of(129_000, "KRW"),
				CATEGORY_ID,
				SUPPLIER_ID,
				NOW,
				"corr-save-001");
		product.pullDomainEvents();

		productRepository.save(product);

		Product loaded = productRepository.findById(PRODUCT_ID).orElseThrow();

		assertEquals("기계식 키보드", loaded.getName().value());
		assertEquals(ProductStatus.DRAFT, loaded.getStatus());
		assertEquals(0L, loaded.getVersion());
		assertTrue(productRepository.existsById(PRODUCT_ID));
	}

	@Test
	void shouldPersistProductChangesWithOptimisticLockVersion() {
		ProductId productId = ProductId.of("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
		Product product = Product.create(
				productId,
				new ProductName("무선 이어폰"),
				ProductDescription.empty(),
				Money.of(89_000, "KRW"),
				CATEGORY_ID,
				SUPPLIER_ID,
				NOW,
				"corr-save-002");
		product.pullDomainEvents();
		productRepository.save(product);

		Product loaded = productRepository.findById(productId).orElseThrow();
		loaded.changePrice(0L, Money.of(79_000, "KRW"), NOW, "corr-save-002");
		loaded.pullDomainEvents();
		productRepository.save(loaded);

		Product reloaded = productRepository.findById(productId).orElseThrow();
		assertEquals(Money.of(79_000, "KRW"), reloaded.getPrice());
		assertEquals(1L, reloaded.getVersion());
	}

	@Test
	void shouldPersistImagesKeywordsAndTags() {
		ProductId productId = ProductId.of("cccccccc-cccc-cccc-cccc-cccccccccccc");
		Product product = Product.create(
				productId,
				new ProductName("스마트 워치"),
				new ProductDescription("방수 지원"),
				Money.of(299_000, "KRW"),
				CATEGORY_ID,
				SUPPLIER_ID,
				NOW,
				"corr-save-003");
		product.pullDomainEvents();

		ImageReference image = ImageReference.create(
				"products/" + UUID.randomUUID() + "/watch.png",
				"image/png",
				2048L,
				false,
				NOW);
		product.uploadImage(0L, image, NOW, "corr-save-003");
		product.pullDomainEvents();
		product.requestAiEnrichment(1L, NOW, "corr-save-003");
		product.pullDomainEvents();
		product.completeAiEnrichment(
				2L,
				"stub",
				Set.of(new ProductKeyword("스마트워치")),
				Set.of(new ProductTag("웨어러블")),
				NOW,
				"corr-save-003");
		product.pullDomainEvents();
		productRepository.save(product);

		Product loaded = productRepository.findById(productId).orElseThrow();

		assertEquals(1, loaded.getImages().size());
		assertEquals("스마트워치", loaded.getKeywords().iterator().next().value());
		assertEquals("웨어러블", loaded.getTags().iterator().next().value());
		assertEquals(ProductStatus.REVIEW_REQUIRED, loaded.getStatus());
		assertFalse(loaded.getImages().isEmpty());
	}
}
