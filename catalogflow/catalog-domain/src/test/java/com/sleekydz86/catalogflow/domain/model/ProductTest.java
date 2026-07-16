package com.sleekydz86.catalogflow.domain.model;

import com.sleekydz86.catalogflow.domain.event.DomainEvent;
import com.sleekydz86.catalogflow.domain.event.ProductCreated;
import com.sleekydz86.catalogflow.domain.event.ProductEnrichmentCompleted;
import com.sleekydz86.catalogflow.domain.event.ProductEnrichmentRequested;
import com.sleekydz86.catalogflow.domain.event.ProductImageUploaded;
import com.sleekydz86.catalogflow.domain.event.ProductPriceChanged;
import com.sleekydz86.catalogflow.domain.event.ProductPublished;
import com.sleekydz86.catalogflow.domain.event.ProductSuspended;
import com.sleekydz86.catalogflow.domain.event.ProductUpdated;
import com.sleekydz86.catalogflow.domain.exception.AiEnrichmentNotCompletedException;
import com.sleekydz86.catalogflow.domain.exception.DuplicateAiEnrichmentRequestException;
import com.sleekydz86.catalogflow.domain.exception.InsufficientProductImagesException;
import com.sleekydz86.catalogflow.domain.exception.InvalidPriceException;
import com.sleekydz86.catalogflow.domain.exception.InvalidProductNameException;
import com.sleekydz86.catalogflow.domain.exception.InvalidProductStateException;
import com.sleekydz86.catalogflow.domain.exception.ProductVersionConflictException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.sleekydz86.catalogflow.domain.model.ProductTestFixture.CORRELATION_ID;
import static com.sleekydz86.catalogflow.domain.model.ProductTestFixture.NOW;
import static com.sleekydz86.catalogflow.domain.model.ProductTestFixture.categoryId;
import static com.sleekydz86.catalogflow.domain.model.ProductTestFixture.draftProduct;
import static com.sleekydz86.catalogflow.domain.model.ProductTestFixture.imageReference;
import static com.sleekydz86.catalogflow.domain.model.ProductTestFixture.price;
import static com.sleekydz86.catalogflow.domain.model.ProductTestFixture.productDescription;
import static com.sleekydz86.catalogflow.domain.model.ProductTestFixture.productId;
import static com.sleekydz86.catalogflow.domain.model.ProductTestFixture.productName;
import static com.sleekydz86.catalogflow.domain.model.ProductTestFixture.readyProductWithImage;
import static com.sleekydz86.catalogflow.domain.model.ProductTestFixture.supplierId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest {

	@Nested
	class Creation {

		@Test
		void shouldCreateDraftProductWithDomainEvent() {
			Product product = Product.create(
					productId(),
					productName(),
					productDescription(),
					price(),
					categoryId(),
					supplierId(),
					NOW,
					CORRELATION_ID);

			assertEquals(ProductStatus.DRAFT, product.getStatus());
			assertEquals(AiEnrichmentStatus.NOT_REQUESTED, product.getAiEnrichmentStatus());
			assertEquals(0L, product.getVersion());
			assertTrue(product.getImages().isEmpty());
			assertTrue(product.getKeywords().isEmpty());
			assertTrue(product.getTags().isEmpty());

			List<DomainEvent> events = product.pullDomainEvents();
			assertEquals(1, events.size());
			ProductCreated created = assertInstanceOf(ProductCreated.class, events.getFirst());
			assertEquals(ProductCreated.EVENT_TYPE, created.eventType());
			assertEquals(productId(), created.aggregateId());
			assertEquals(productName(), created.name());
			assertEquals(price(), created.price());
		}

		@Test
		void shouldRejectBlankProductName() {
			assertThrows(InvalidProductNameException.class, () -> new ProductName("   "));
		}
	}

	@Nested
	class PriceValidation {

		@Test
		void shouldRejectNegativePrice() {
			assertThrows(InvalidPriceException.class,
					() -> Money.of(new BigDecimal("-1"), "KRW"));
		}

		@Test
		void shouldAllowZeroPrice() {
			Money zeroPrice = Money.of(0, "KRW");
			assertEquals(0, zeroPrice.amount().compareTo(BigDecimal.ZERO));
		}

		@Test
		void shouldChangePriceForDraftProduct() {
			Product product = draftProduct();

			product.changePrice(0L, Money.of(39_900, "KRW"), NOW, CORRELATION_ID);

			assertEquals(Money.of(39_900, "KRW"), product.getPrice());
			assertEquals(1L, product.getVersion());
			assertInstanceOf(ProductPriceChanged.class, product.pullDomainEvents().getFirst());
		}
	}

	@Nested
	class StateTransition {

		@Test
		void shouldUpdateOnlyInDraftStatus() {
			Product product = draftProduct();
			ProductName updatedName = new ProductName("Updated Mouse");

			product.updateDraft(
					0L,
					updatedName,
					productDescription(),
					categoryId(),
					supplierId(),
					NOW,
					CORRELATION_ID);

			assertEquals(updatedName, product.getName());
			assertEquals(1L, product.getVersion());
		}

		@Test
		void shouldRejectUpdateWhenNotDraft() {
			Product product = readyProductWithImage();

			assertThrows(InvalidProductStateException.class, () -> product.updateDraft(
					4L,
					new ProductName("Blocked Update"),
					productDescription(),
					categoryId(),
					supplierId(),
					NOW,
					CORRELATION_ID));
		}

		@Test
		void shouldSuspendPublishedProduct() {
			Product product = readyProductWithImage();
			product.publish(4L, NOW, CORRELATION_ID);
			product.pullDomainEvents();

			product.suspend(5L, "policy violation", NOW, CORRELATION_ID);

			assertEquals(ProductStatus.SUSPENDED, product.getStatus());
			assertInstanceOf(ProductSuspended.class, product.pullDomainEvents().getFirst());
		}

		@Test
		void shouldRejectPriceChangeForSuspendedProduct() {
			Product product = readyProductWithImage();
			product.publish(4L, NOW, CORRELATION_ID);
			product.pullDomainEvents();
			product.suspend(5L, "policy violation", NOW, CORRELATION_ID);
			product.pullDomainEvents();

			assertThrows(InvalidProductStateException.class,
					() -> product.changePrice(6L, Money.of(10_000, "KRW"), NOW, CORRELATION_ID));
		}
	}

	@Nested
	class PublishRules {

		@Test
		void shouldPublishReadyProductWithImage() {
			Product product = readyProductWithImage();

			product.publish(4L, NOW, CORRELATION_ID);

			assertEquals(ProductStatus.PUBLISHED, product.getStatus());
			assertEquals(NOW, product.getPublishedAt());
			assertInstanceOf(ProductPublished.class, product.pullDomainEvents().getFirst());
		}

		@Test
		void shouldRejectPublishWithoutImage() {
			Product product = draftProduct();
			product.requestAiEnrichment(0L, NOW, CORRELATION_ID);
			product.pullDomainEvents();
			product.completeAiEnrichment(
					1L,
					"stub",
					Set.of(new ProductKeyword("mouse")),
					Set.of(new ProductTag("electronics")),
					NOW,
					CORRELATION_ID);
			product.pullDomainEvents();
			product.approveAiEnrichment(2L, NOW, CORRELATION_ID);
			product.pullDomainEvents();

			assertThrows(InsufficientProductImagesException.class,
					() -> product.publish(3L, NOW, CORRELATION_ID));
		}

		@Test
		void shouldRejectPublishWhenNotReady() {
			Product product = draftProduct();

			assertThrows(InvalidProductStateException.class,
					() -> product.publish(0L, NOW, CORRELATION_ID));
		}
	}

	@Nested
	class AiEnrichment {

		@Test
		void shouldRequestAiEnrichmentOnce() {
			Product product = draftProduct();

			product.requestAiEnrichment(0L, NOW, CORRELATION_ID);

			assertEquals(ProductStatus.ENRICHMENT_PENDING, product.getStatus());
			assertEquals(AiEnrichmentStatus.REQUESTED, product.getAiEnrichmentStatus());
			assertInstanceOf(ProductEnrichmentRequested.class, product.pullDomainEvents().getFirst());
		}

		@Test
		void shouldRejectDuplicateAiEnrichmentRequest() {
			Product product = draftProduct();
			product.requestAiEnrichment(0L, NOW, CORRELATION_ID);
			product.pullDomainEvents();

			assertThrows(DuplicateAiEnrichmentRequestException.class,
					() -> product.requestAiEnrichment(1L, NOW, CORRELATION_ID));
		}

		@Test
		void shouldCompleteAiEnrichmentAndRequireApproval() {
			Product product = draftProduct();
			product.requestAiEnrichment(0L, NOW, CORRELATION_ID);
			product.pullDomainEvents();

			product.completeAiEnrichment(
					1L,
					"stub",
					Set.of(new ProductKeyword("mouse")),
					Set.of(new ProductTag("electronics")),
					NOW,
					CORRELATION_ID);

			assertEquals(ProductStatus.REVIEW_REQUIRED, product.getStatus());
			assertEquals(AiEnrichmentStatus.COMPLETED, product.getAiEnrichmentStatus());
			assertEquals(Set.of(new ProductKeyword("mouse")), product.getKeywords());
			assertInstanceOf(ProductEnrichmentCompleted.class, product.pullDomainEvents().getFirst());
		}

		@Test
		void shouldApproveAiEnrichmentToReady() {
			Product product = draftProduct();
			product.requestAiEnrichment(0L, NOW, CORRELATION_ID);
			product.pullDomainEvents();
			product.completeAiEnrichment(
					1L,
					"stub",
					Set.of(new ProductKeyword("mouse")),
					Set.of(new ProductTag("electronics")),
					NOW,
					CORRELATION_ID);
			product.pullDomainEvents();

			product.approveAiEnrichment(2L, NOW, CORRELATION_ID);

			assertEquals(ProductStatus.READY, product.getStatus());
			assertInstanceOf(ProductUpdated.class, product.pullDomainEvents().getFirst());
		}

		@Test
		void shouldRejectApprovalWithoutCompletedEnrichment() {
			Product product = Product.reconstitute(
					productId(),
					productName(),
					productDescription(),
					price(),
					ProductStatus.REVIEW_REQUIRED,
					categoryId(),
					supplierId(),
					List.of(imageReference()),
					AiEnrichmentStatus.REQUESTED,
					Set.of(),
					Set.of(),
					2L,
					NOW,
					NOW,
					null);

			assertThrows(AiEnrichmentNotCompletedException.class,
					() -> product.approveAiEnrichment(2L, NOW, CORRELATION_ID));
		}
	}

	@Nested
	class OptimisticLock {

		@Test
		void shouldRejectUpdateWithStaleVersion() {
			Product product = draftProduct();

			assertThrows(ProductVersionConflictException.class, () -> product.updateDraft(
					99L,
					new ProductName("Stale Update"),
					productDescription(),
					categoryId(),
					supplierId(),
					NOW,
					CORRELATION_ID));
		}

		@Test
		void shouldIncrementVersionOnEachMutation() {
			Product product = draftProduct();

			product.uploadImage(0L, imageReference(), NOW, CORRELATION_ID);

			assertEquals(1L, product.getVersion());
			List<DomainEvent> events = product.pullDomainEvents();
			assertEquals(1, events.size());
			assertInstanceOf(ProductImageUploaded.class, events.getFirst());
		}
	}
}
