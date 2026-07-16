package com.sleekydz86.catalogflow.application.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.out.ProductViewStore;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventEnvelope;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventPayloads;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.stereotype.Service;

@Service
public class ProductViewProjectionService {

	private final ProductViewStore productViewStore;

	public ProductViewProjectionService(ProductViewStore productViewStore) {
		this.productViewStore = productViewStore;
	}

	public Optional<ProductView> project(IntegrationEventEnvelope envelope) {
		Optional<ProductView> existing = productViewStore.findByProductId(envelope.aggregateId());
		if (existing.isPresent() && envelope.aggregateVersion() <= existing.get().getVersion()) {
			return Optional.empty();
		}
		if (CatalogEventTypes.PRODUCT_CREATED.equals(envelope.eventType())) {
			return Optional.of(applyProductCreated(envelope));
		}
		ProductView view = existing.orElseThrow(() -> new ApplicationException(
				"상품 조회 모델이 없어 이벤트를 적용할 수 없습니다: " + envelope.aggregateId()));
		switch (envelope.eventType()) {
			case CatalogEventTypes.PRODUCT_UPDATED -> applyProductUpdated(view, envelope);
			case CatalogEventTypes.PRODUCT_PRICE_CHANGED -> applyProductPriceChanged(view, envelope);
			case CatalogEventTypes.PRODUCT_IMAGE_UPLOADED -> applyProductImageUploaded(view, envelope);
			case CatalogEventTypes.PRODUCT_ENRICHMENT_COMPLETED -> applyProductEnrichmentCompleted(view, envelope);
			case CatalogEventTypes.PRODUCT_PUBLISHED -> applyProductPublished(view, envelope);
			case CatalogEventTypes.PRODUCT_SUSPENDED, CatalogEventTypes.PRODUCT_DISCONTINUED ->
					applyProductStatusChanged(view, envelope);
			default -> throw new ApplicationException("지원하지 않는 상품 이벤트 유형입니다: " + envelope.eventType());
		}
		view.setVersion(envelope.aggregateVersion());
		productViewStore.save(view);
		return Optional.of(view);
	}

	private ProductView applyProductCreated(IntegrationEventEnvelope envelope) {
		IntegrationEventPayloads.ProductCreatedData data = IntegrationEventPayloads.readProductCreated(envelope.payload());
		ProductView view = ProductView.create(envelope.aggregateId());
		view.setName(data.name());
		view.setSummary(truncate(data.description(), 200));
		view.setDescription(data.description());
		view.setPrice(data.priceAmount());
		view.setCurrency(data.priceCurrency());
		view.setStatus(data.status());
		view.setCategoryId(data.categoryId());
		view.setSupplierId(data.supplierId());
		view.setSupplierName(data.supplierName());
		view.setCreatedAt(data.createdAt());
		view.setUpdatedAt(data.updatedAt());
		view.setVersion(envelope.aggregateVersion());
		productViewStore.save(view);
		return view;
	}

	private void applyProductUpdated(ProductView view, IntegrationEventEnvelope envelope) {
		IntegrationEventPayloads.ProductUpdatedData data = IntegrationEventPayloads.readProductUpdated(envelope.payload());
		view.setName(data.name());
		view.setSummary(truncate(data.description(), 200));
		view.setDescription(data.description());
		view.setCategoryId(data.categoryId());
		view.setSupplierId(data.supplierId());
		view.setSupplierName(data.supplierName());
		view.setStatus(data.status());
		view.setUpdatedAt(data.updatedAt());
	}

	private void applyProductPriceChanged(ProductView view, IntegrationEventEnvelope envelope) {
		IntegrationEventPayloads.ProductPriceChangedData data =
				IntegrationEventPayloads.readProductPriceChanged(envelope.payload());
		view.setPrice(data.priceAmount());
		view.setCurrency(data.priceCurrency());
		view.setUpdatedAt(data.updatedAt());
	}

	private void applyProductImageUploaded(ProductView view, IntegrationEventEnvelope envelope) {
		IntegrationEventPayloads.ProductImageUploadedData data =
				IntegrationEventPayloads.readProductImageUploaded(envelope.payload());
		ArrayList<String> imageUrls = new ArrayList<>(view.getImageUrls());
		String imageUrl = data.storageKey();
		if (!imageUrls.contains(imageUrl)) {
			imageUrls.add(imageUrl);
		}
		view.setImageUrls(imageUrls);
		view.setUpdatedAt(data.updatedAt());
	}

	private void applyProductEnrichmentCompleted(ProductView view, IntegrationEventEnvelope envelope) {
		IntegrationEventPayloads.ProductEnrichmentCompletedData data =
				IntegrationEventPayloads.readProductEnrichmentCompleted(envelope.payload());
		view.setSummary(data.summary());
		view.setDescription(data.generatedDescription());
		view.setKeywords(data.keywords());
		view.setTags(data.tags());
		view.setAiGenerated(true);
		view.setAiModel(data.modelName());
		view.setStatus(data.status());
		view.setUpdatedAt(data.updatedAt());
	}

	private void applyProductPublished(ProductView view, IntegrationEventEnvelope envelope) {
		IntegrationEventPayloads.ProductPublishedData data =
				IntegrationEventPayloads.readProductPublished(envelope.payload());
		view.setStatus(data.status());
		view.setPublishedAt(data.publishedAt());
		view.setUpdatedAt(data.updatedAt());
	}

	private void applyProductStatusChanged(ProductView view, IntegrationEventEnvelope envelope) {
		IntegrationEventPayloads.ProductStatusChangedData data =
				IntegrationEventPayloads.readProductStatusChanged(envelope.payload());
		view.setStatus(data.status());
		view.setUpdatedAt(data.updatedAt());
	}

	private String truncate(String value, int maxLength) {
		if (value == null || value.length() <= maxLength) {
			return value == null ? "" : value;
		}
		return value.substring(0, maxLength);
	}
}
