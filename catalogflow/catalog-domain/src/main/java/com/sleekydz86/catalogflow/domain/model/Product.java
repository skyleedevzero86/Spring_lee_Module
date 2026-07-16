package com.sleekydz86.catalogflow.domain.model;

import com.sleekydz86.catalogflow.domain.event.DomainEvent;
import com.sleekydz86.catalogflow.domain.event.ProductCreated;
import com.sleekydz86.catalogflow.domain.event.ProductDiscontinued;
import com.sleekydz86.catalogflow.domain.event.ProductEnrichmentCompleted;
import com.sleekydz86.catalogflow.domain.event.ProductEnrichmentFailed;
import com.sleekydz86.catalogflow.domain.event.ProductEnrichmentRequested;
import com.sleekydz86.catalogflow.domain.event.ProductImageUploaded;
import com.sleekydz86.catalogflow.domain.event.ProductPriceChanged;
import com.sleekydz86.catalogflow.domain.event.ProductPublished;
import com.sleekydz86.catalogflow.domain.event.ProductSuspended;
import com.sleekydz86.catalogflow.domain.event.ProductUpdated;
import com.sleekydz86.catalogflow.domain.exception.AiEnrichmentNotCompletedException;
import com.sleekydz86.catalogflow.domain.exception.DuplicateAiEnrichmentRequestException;
import com.sleekydz86.catalogflow.domain.exception.InsufficientProductImagesException;
import com.sleekydz86.catalogflow.domain.exception.InvalidProductStateException;
import com.sleekydz86.catalogflow.domain.exception.ProductVersionConflictException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Product {

	private final ProductId id;
	private ProductName name;
	private ProductDescription description;
	private Money price;
	private ProductStatus status;
	private CategoryId categoryId;
	private SupplierId supplierId;
	private final List<ImageReference> images;
	private AiEnrichmentStatus aiEnrichmentStatus;
	private final Set<ProductKeyword> keywords;
	private final Set<ProductTag> tags;
	private long version;
	private final Instant createdAt;
	private Instant updatedAt;
	private Instant publishedAt;
	private final List<DomainEvent> domainEvents;

	private Product(
			ProductId id,
			ProductName name,
			ProductDescription description,
			Money price,
			ProductStatus status,
			CategoryId categoryId,
			SupplierId supplierId,
			List<ImageReference> images,
			AiEnrichmentStatus aiEnrichmentStatus,
			Set<ProductKeyword> keywords,
			Set<ProductTag> tags,
			long version,
			Instant createdAt,
			Instant updatedAt,
			Instant publishedAt) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = Objects.requireNonNull(name, "name");
		this.description = Objects.requireNonNull(description, "description");
		this.price = Objects.requireNonNull(price, "price");
		this.status = Objects.requireNonNull(status, "status");
		this.categoryId = Objects.requireNonNull(categoryId, "categoryId");
		this.supplierId = Objects.requireNonNull(supplierId, "supplierId");
		this.images = new ArrayList<>(images);
		this.aiEnrichmentStatus = Objects.requireNonNull(aiEnrichmentStatus, "aiEnrichmentStatus");
		this.keywords = new LinkedHashSet<>(keywords);
		this.tags = new LinkedHashSet<>(tags);
		this.version = version;
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
		this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
		this.publishedAt = publishedAt;
		this.domainEvents = new ArrayList<>();
	}

	public static Product create(
			ProductId id,
			ProductName name,
			ProductDescription description,
			Money price,
			CategoryId categoryId,
			SupplierId supplierId,
			Instant now,
			String correlationId) {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(now, "now");

		Product product = new Product(
				id,
				name,
				description,
				price,
				ProductStatus.DRAFT,
				categoryId,
				supplierId,
				List.of(),
				AiEnrichmentStatus.NOT_REQUESTED,
				Set.of(),
				Set.of(),
				0L,
				now,
				now,
				null);

		product.register(new ProductCreated(
				null,
				id,
				product.version,
				now,
				correlationId,
				"",
				ProductCreated.CURRENT_SCHEMA_VERSION,
				name,
				description,
				price,
				categoryId,
				supplierId,
				ProductStatus.DRAFT));

		return product;
	}

	public static Product reconstitute(
			ProductId id,
			ProductName name,
			ProductDescription description,
			Money price,
			ProductStatus status,
			CategoryId categoryId,
			SupplierId supplierId,
			List<ImageReference> images,
			AiEnrichmentStatus aiEnrichmentStatus,
			Set<ProductKeyword> keywords,
			Set<ProductTag> tags,
			long version,
			Instant createdAt,
			Instant updatedAt,
			Instant publishedAt) {
		return new Product(
				id,
				name,
				description,
				price,
				status,
				categoryId,
				supplierId,
				images,
				aiEnrichmentStatus,
				keywords,
				tags,
				version,
				createdAt,
				updatedAt,
				publishedAt);
	}

	public void updateDraft(
			long expectedVersion,
			ProductName newName,
			ProductDescription newDescription,
			CategoryId newCategoryId,
			SupplierId newSupplierId,
			Instant now,
			String correlationId) {
		assertVersion(expectedVersion);
		assertStatus(ProductStatus.DRAFT, "DRAFT 상태에서만 상품을 수정할 수 있습니다");

		this.name = Objects.requireNonNull(newName, "newName");
		this.description = Objects.requireNonNull(newDescription, "newDescription");
		this.categoryId = Objects.requireNonNull(newCategoryId, "newCategoryId");
		this.supplierId = Objects.requireNonNull(newSupplierId, "newSupplierId");
		touch(now);

		register(new ProductUpdated(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductUpdated.CURRENT_SCHEMA_VERSION,
				name,
				description,
				categoryId,
				supplierId,
				status));
	}

	public void changePrice(long expectedVersion, Money newPrice, Instant now, String correlationId) {
		assertVersion(expectedVersion);
		Objects.requireNonNull(newPrice, "newPrice");
		Objects.requireNonNull(now, "now");

		if (status == ProductStatus.SUSPENDED) {
			throw new InvalidProductStateException("판매 중지된 상품의 가격은 변경할 수 없습니다");
		}
		if (status == ProductStatus.DISCONTINUED) {
			throw new InvalidProductStateException("단종된 상품의 가격은 변경할 수 없습니다");
		}

		Money previousPrice = this.price;
		this.price = newPrice;
		touch(now);

		register(new ProductPriceChanged(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductPriceChanged.CURRENT_SCHEMA_VERSION,
				previousPrice,
				newPrice));
	}

	public void changeCategory(long expectedVersion, CategoryId newCategoryId, Instant now, String correlationId) {
		assertVersion(expectedVersion);
		assertStatus(ProductStatus.DRAFT, "DRAFT 상태에서만 카테고리를 변경할 수 있습니다");

		this.categoryId = Objects.requireNonNull(newCategoryId, "newCategoryId");
		touch(now);

		register(new ProductUpdated(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductUpdated.CURRENT_SCHEMA_VERSION,
				name,
				description,
				categoryId,
				supplierId,
				status));
	}

	public void uploadImage(long expectedVersion, ImageReference imageReference, Instant now, String correlationId) {
		assertVersion(expectedVersion);
		Objects.requireNonNull(imageReference, "imageReference");
		Objects.requireNonNull(now, "now");

		if (status == ProductStatus.DISCONTINUED) {
			throw new InvalidProductStateException("단종된 상품에는 이미지를 추가할 수 없습니다");
		}

		images.add(imageReference);
		touch(now);

		register(new ProductImageUploaded(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductImageUploaded.CURRENT_SCHEMA_VERSION,
				imageReference));
	}

	public void removeImage(long expectedVersion, String imageId, Instant now, String correlationId) {
		assertVersion(expectedVersion);
		Objects.requireNonNull(imageId, "imageId");
		Objects.requireNonNull(now, "now");

		if (status != ProductStatus.DRAFT) {
			throw new InvalidProductStateException("DRAFT 상태에서만 이미지를 삭제할 수 있습니다");
		}

		boolean removed = images.removeIf(image -> image.imageId().equals(imageId));
		if (!removed) {
			throw new InvalidProductStateException("이미지를 찾을 수 없습니다: " + imageId);
		}

		touch(now);

		register(new ProductUpdated(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductUpdated.CURRENT_SCHEMA_VERSION,
				name,
				description,
				categoryId,
				supplierId,
				status));
	}

	public void requestAiEnrichment(long expectedVersion, Instant now, String correlationId) {
		assertVersion(expectedVersion);
		Objects.requireNonNull(now, "now");

		if (aiEnrichmentStatus == AiEnrichmentStatus.REQUESTED
				|| aiEnrichmentStatus == AiEnrichmentStatus.PROCESSING) {
			throw new DuplicateAiEnrichmentRequestException("AI 가공이 이미 진행 중입니다");
		}

		if (status == ProductStatus.DISCONTINUED) {
			throw new InvalidProductStateException("단종된 상품은 AI 가공을 요청할 수 없습니다");
		}

		this.aiEnrichmentStatus = AiEnrichmentStatus.REQUESTED;
		this.status = ProductStatus.ENRICHMENT_PENDING;
		touch(now);

		register(new ProductEnrichmentRequested(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductEnrichmentRequested.CURRENT_SCHEMA_VERSION));
	}

	public void markAiEnrichmentProcessing(long expectedVersion, Instant now, String correlationId) {
		assertVersion(expectedVersion);
		Objects.requireNonNull(now, "now");

		if (aiEnrichmentStatus != AiEnrichmentStatus.REQUESTED) {
			throw new InvalidProductStateException("AI 가공을 먼저 요청해야 처리할 수 있습니다");
		}

		this.aiEnrichmentStatus = AiEnrichmentStatus.PROCESSING;
		touch(now);
	}

	public void completeAiEnrichment(
			long expectedVersion,
			String modelName,
			Set<ProductKeyword> enrichedKeywords,
			Set<ProductTag> enrichedTags,
			Instant now,
			String correlationId) {
		assertVersion(expectedVersion);
		Objects.requireNonNull(enrichedKeywords, "enrichedKeywords");
		Objects.requireNonNull(enrichedTags, "enrichedTags");
		Objects.requireNonNull(now, "now");

		if (aiEnrichmentStatus != AiEnrichmentStatus.REQUESTED
				&& aiEnrichmentStatus != AiEnrichmentStatus.PROCESSING) {
			throw new InvalidProductStateException("진행 중인 AI 가공이 없습니다");
		}

		this.keywords.clear();
		this.keywords.addAll(enrichedKeywords);
		this.tags.clear();
		this.tags.addAll(enrichedTags);
		this.aiEnrichmentStatus = AiEnrichmentStatus.COMPLETED;
		this.status = ProductStatus.REVIEW_REQUIRED;
		touch(now);

		register(new ProductEnrichmentCompleted(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductEnrichmentCompleted.CURRENT_SCHEMA_VERSION,
				modelName == null ? "" : modelName));
	}

	public void failAiEnrichment(long expectedVersion, String reason, Instant now, String correlationId) {
		assertVersion(expectedVersion);
		Objects.requireNonNull(now, "now");

		if (aiEnrichmentStatus != AiEnrichmentStatus.REQUESTED
				&& aiEnrichmentStatus != AiEnrichmentStatus.PROCESSING) {
			throw new InvalidProductStateException("진행 중인 AI 가공이 없습니다");
		}

		this.aiEnrichmentStatus = AiEnrichmentStatus.FAILED;
		this.status = ProductStatus.DRAFT;
		touch(now);

		register(new ProductEnrichmentFailed(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductEnrichmentFailed.CURRENT_SCHEMA_VERSION,
				reason == null ? "" : reason));
	}

	public void approveAiEnrichment(long expectedVersion, Instant now, String correlationId) {
		assertVersion(expectedVersion);
		Objects.requireNonNull(now, "now");

		if (status != ProductStatus.REVIEW_REQUIRED) {
			throw new InvalidProductStateException("승인하려면 REVIEW_REQUIRED 상태여야 합니다");
		}
		if (aiEnrichmentStatus != AiEnrichmentStatus.COMPLETED) {
			throw new AiEnrichmentNotCompletedException("승인하려면 AI 가공이 완료되어야 합니다");
		}

		this.status = ProductStatus.READY;
		touch(now);

		register(new ProductUpdated(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductUpdated.CURRENT_SCHEMA_VERSION,
				name,
				description,
				categoryId,
				supplierId,
				status));
	}

	public void publish(long expectedVersion, Instant now, String correlationId) {
		assertVersion(expectedVersion);
		Objects.requireNonNull(now, "now");

		if (status != ProductStatus.READY) {
			throw new InvalidProductStateException("READY 상태의 상품만 공개할 수 있습니다");
		}
		if (images.isEmpty()) {
			throw new InsufficientProductImagesException("공개하려면 이미지가 최소 한 개 필요합니다");
		}

		this.status = ProductStatus.PUBLISHED;
		this.publishedAt = now;
		touch(now);

		register(new ProductPublished(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductPublished.CURRENT_SCHEMA_VERSION,
				now));
	}

	public void suspend(long expectedVersion, String reason, Instant now, String correlationId) {
		assertVersion(expectedVersion);
		Objects.requireNonNull(now, "now");

		if (status != ProductStatus.PUBLISHED) {
			throw new InvalidProductStateException("PUBLISHED 상태의 상품만 판매 중지할 수 있습니다");
		}

		this.status = ProductStatus.SUSPENDED;
		touch(now);

		register(new ProductSuspended(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductSuspended.CURRENT_SCHEMA_VERSION,
				reason == null ? "" : reason));
	}

	public void discontinue(long expectedVersion, String reason, Instant now, String correlationId) {
		assertVersion(expectedVersion);
		Objects.requireNonNull(now, "now");

		if (status == ProductStatus.DISCONTINUED) {
			throw new InvalidProductStateException("이미 단종된 상품입니다");
		}

		this.status = ProductStatus.DISCONTINUED;
		touch(now);

		register(new ProductDiscontinued(
				null,
				id,
				version,
				now,
				correlationId,
				"",
				ProductDiscontinued.CURRENT_SCHEMA_VERSION,
				reason == null ? "" : reason));
	}

	public List<DomainEvent> pullDomainEvents() {
		List<DomainEvent> events = List.copyOf(domainEvents);
		domainEvents.clear();
		return events;
	}

	public ProductId getId() {
		return id;
	}

	public ProductName getName() {
		return name;
	}

	public ProductDescription getDescription() {
		return description;
	}

	public Money getPrice() {
		return price;
	}

	public ProductStatus getStatus() {
		return status;
	}

	public CategoryId getCategoryId() {
		return categoryId;
	}

	public SupplierId getSupplierId() {
		return supplierId;
	}

	public List<ImageReference> getImages() {
		return List.copyOf(images);
	}

	public AiEnrichmentStatus getAiEnrichmentStatus() {
		return aiEnrichmentStatus;
	}

	public Set<ProductKeyword> getKeywords() {
		return Set.copyOf(keywords);
	}

	public Set<ProductTag> getTags() {
		return Set.copyOf(tags);
	}

	public long getVersion() {
		return version;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	private void assertVersion(long expectedVersion) {
		if (version != expectedVersion) {
			throw new ProductVersionConflictException(expectedVersion, version);
		}
	}

	private void assertStatus(ProductStatus requiredStatus, String message) {
		if (status != requiredStatus) {
			throw new InvalidProductStateException(message);
		}
	}

	private void touch(Instant now) {
		version++;
		updatedAt = now;
	}

	private void register(DomainEvent event) {
		domainEvents.add(event);
	}
}
