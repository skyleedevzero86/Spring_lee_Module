package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.AiEnrichmentResultEntity;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductEntity;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductKeywordEntity;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductTagEntity;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.SupplierEntity;
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
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventPayloadFactory {

	private final SupplierJpaRepository supplierJpaRepository;
	private final ProductJpaRepository productJpaRepository;
	private final AiEnrichmentResultJpaRepository aiEnrichmentResultJpaRepository;

	public OutboxEventPayloadFactory(
			SupplierJpaRepository supplierJpaRepository,
			ProductJpaRepository productJpaRepository,
			AiEnrichmentResultJpaRepository aiEnrichmentResultJpaRepository) {
		this.supplierJpaRepository = supplierJpaRepository;
		this.productJpaRepository = productJpaRepository;
		this.aiEnrichmentResultJpaRepository = aiEnrichmentResultJpaRepository;
	}

	public String createPayload(DomainEvent event) {
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		appendCommonFields(builder, event);
		appendEventData(builder, event);
		builder.append("}");
		return builder.toString();
	}

	private void appendCommonFields(StringBuilder builder, DomainEvent event) {
		appendField(builder, "eventId", event.eventId().toString(), true);
		appendField(builder, "eventType", event.eventType(), false);
		appendField(builder, "aggregateId", event.aggregateId().value().toString(), false);
		appendNumberField(builder, "aggregateVersion", event.aggregateVersion(), false);
		appendField(builder, "occurredAt", event.occurredAt().toString(), false);
		appendField(builder, "correlationId", event.correlationId(), false);
		appendField(builder, "causationId", event.causationId(), false);
		appendNumberField(builder, "schemaVersion", event.schemaVersion(), false);
	}

	private void appendEventData(StringBuilder builder, DomainEvent event) {
		if (event instanceof ProductCreated created) {
			appendProductCreated(builder, created);
			return;
		}
		if (event instanceof ProductUpdated updated) {
			appendProductUpdated(builder, updated);
			return;
		}
		if (event instanceof ProductPriceChanged priceChanged) {
			appendProductPriceChanged(builder, priceChanged);
			return;
		}
		if (event instanceof ProductImageUploaded imageUploaded) {
			appendProductImageUploaded(builder, imageUploaded);
			return;
		}
		if (event instanceof ProductEnrichmentRequested enrichmentRequested) {
			appendProductEnrichmentRequested(builder, enrichmentRequested);
			return;
		}
		if (event instanceof ProductEnrichmentCompleted enrichmentCompleted) {
			appendProductEnrichmentCompleted(builder, enrichmentCompleted);
			return;
		}
		if (event instanceof ProductEnrichmentFailed enrichmentFailed) {
			appendProductEnrichmentFailed(builder, enrichmentFailed);
			return;
		}
		if (event instanceof ProductPublished published) {
			appendProductPublished(builder, published);
			return;
		}
		if (event instanceof ProductSuspended suspended) {
			appendProductStatusChanged(builder, "SUSPENDED", suspended.occurredAt());
			return;
		}
		if (event instanceof ProductDiscontinued discontinued) {
			appendProductStatusChanged(builder, "DISCONTINUED", discontinued.occurredAt());
		}
	}

	private void appendProductCreated(StringBuilder builder, ProductCreated event) {
		appendField(builder, "name", event.name().value(), false);
		appendField(builder, "description", event.description().value(), false);
		appendDecimalField(builder, "priceAmount", event.price().amount(), false);
		appendField(builder, "priceCurrency", event.price().currency(), false);
		appendField(builder, "categoryId", event.categoryId().value().toString(), false);
		appendField(builder, "supplierId", event.supplierId().value().toString(), false);
		appendField(builder, "supplierName", resolveSupplierName(event.supplierId().value()), false);
		appendField(builder, "status", event.status().name(), false);
		appendField(builder, "createdAt", event.occurredAt().toString(), false);
		appendField(builder, "updatedAt", event.occurredAt().toString(), false);
	}

	private void appendProductUpdated(StringBuilder builder, ProductUpdated event) {
		appendField(builder, "name", event.name().value(), false);
		appendField(builder, "description", event.description().value(), false);
		appendField(builder, "categoryId", event.categoryId().value().toString(), false);
		appendField(builder, "supplierId", event.supplierId().value().toString(), false);
		appendField(builder, "supplierName", resolveSupplierName(event.supplierId().value()), false);
		appendField(builder, "status", event.status().name(), false);
		appendField(builder, "updatedAt", event.occurredAt().toString(), false);
	}

	private void appendProductPriceChanged(StringBuilder builder, ProductPriceChanged event) {
		appendDecimalField(builder, "priceAmount", event.newPrice().amount(), false);
		appendField(builder, "priceCurrency", event.newPrice().currency(), false);
		appendField(builder, "updatedAt", event.occurredAt().toString(), false);
	}

	private void appendProductImageUploaded(StringBuilder builder, ProductImageUploaded event) {
		appendField(builder, "imageId", event.imageReference().imageId(), false);
		appendField(builder, "storageKey", event.imageReference().storageKey(), false);
		appendField(builder, "contentType", event.imageReference().contentType(), false);
		appendField(builder, "updatedAt", event.occurredAt().toString(), false);
	}

	private void appendProductEnrichmentRequested(StringBuilder builder, ProductEnrichmentRequested event) {
		ProductEntity product = productJpaRepository.findById(event.aggregateId().value())
				.orElseThrow(() -> new ApplicationException("AI 가공 요청 상품을 찾을 수 없습니다"));
		appendField(builder, "name", product.getName(), false);
		appendField(builder, "description", product.getDescription(), false);
		appendField(builder, "categoryId", product.getCategoryId().toString(), false);
		appendField(builder, "supplierId", product.getSupplierId().toString(), false);
		appendField(builder, "supplierName", resolveSupplierName(product.getSupplierId()), false);
		appendField(builder, "status", product.getStatus(), false);
		appendField(builder, "updatedAt", event.occurredAt().toString(), false);
	}

	private void appendProductEnrichmentCompleted(StringBuilder builder, ProductEnrichmentCompleted event) {
		ProductEntity product = productJpaRepository.findById(event.aggregateId().value())
				.orElseThrow(() -> new ApplicationException("AI 가공 완료 상품을 찾을 수 없습니다"));
		var enrichmentResult = aiEnrichmentResultJpaRepository
				.findFirstByProductIdOrderByCreatedAtDesc(event.aggregateId().value());
		String summary = enrichmentResult.map(AiEnrichmentResultEntity::getSummary)
				.filter(value -> value != null && !value.isBlank())
				.orElseGet(() -> truncate(product.getDescription(), 200));
		String generatedDescription = enrichmentResult.map(AiEnrichmentResultEntity::getGeneratedDescription)
				.filter(value -> value != null && !value.isBlank())
				.orElseGet(product::getDescription);
		String recommendedCategory = enrichmentResult.map(AiEnrichmentResultEntity::getRecommendedCategory).orElse("");
		String warnings = enrichmentResult.map(AiEnrichmentResultEntity::getWarnings).orElse("");
		boolean requiresHumanReview = enrichmentResult.map(AiEnrichmentResultEntity::isRequiresHumanReview).orElse(true);
		String confidence = enrichmentResult
				.map(AiEnrichmentResultEntity::getConfidence)
				.map(value -> value == null ? "0" : value.toPlainString())
				.orElse("0");
		String promptVersion = enrichmentResult.map(AiEnrichmentResultEntity::getPromptVersion).orElse("");
		appendField(builder, "summary", summary, false);
		appendField(builder, "generatedDescription", generatedDescription, false);
		appendField(builder, "modelName", event.modelName(), false);
		appendStringArrayField(builder, "keywords", readKeywords(product), false);
		appendStringArrayField(builder, "tags", readTags(product), false);
		appendField(builder, "recommendedCategory", recommendedCategory, false);
		appendField(builder, "warnings", warnings, false);
		builder.append(",\"requiresHumanReview\":").append(requiresHumanReview);
		builder.append(",\"confidence\":").append(confidence);
		appendField(builder, "promptVersion", promptVersion, false);
		appendField(builder, "status", product.getStatus(), false);
		appendField(builder, "updatedAt", event.occurredAt().toString(), false);
	}

	private void appendProductEnrichmentFailed(StringBuilder builder, ProductEnrichmentFailed event) {
		appendField(builder, "reason", event.reason(), false);
		appendField(builder, "status", "DRAFT", false);
		appendField(builder, "updatedAt", event.occurredAt().toString(), false);
	}

	private void appendProductPublished(StringBuilder builder, ProductPublished event) {
		appendField(builder, "status", "PUBLISHED", false);
		appendField(builder, "publishedAt", event.publishedAt().toString(), false);
		appendField(builder, "updatedAt", event.occurredAt().toString(), false);
	}

	private void appendProductStatusChanged(StringBuilder builder, String status, Instant occurredAt) {
		appendField(builder, "status", status, false);
		appendField(builder, "updatedAt", occurredAt.toString(), false);
	}

	private String resolveSupplierName(UUID supplierId) {
		return supplierJpaRepository.findById(supplierId)
				.map(SupplierEntity::getName)
				.orElse("");
	}

	private List<String> readKeywords(ProductEntity product) {
		List<String> keywords = new ArrayList<>();
		for (ProductKeywordEntity keyword : product.getKeywords()) {
			keywords.add(keyword.getKeyword());
		}
		return keywords;
	}

	private List<String> readTags(ProductEntity product) {
		List<String> tags = new ArrayList<>();
		for (ProductTagEntity tag : product.getTags()) {
			tags.add(tag.getTag());
		}
		return tags;
	}

	private String truncate(String value, int maxLength) {
		if (value == null || value.length() <= maxLength) {
			return value == null ? "" : value;
		}
		return value.substring(0, maxLength);
	}

	private void appendField(StringBuilder builder, String name, String value, boolean first) {
		if (!first) {
			builder.append(",");
		}
		builder.append("\"").append(name).append("\":\"").append(escape(value)).append("\"");
	}

	private void appendNumberField(StringBuilder builder, String name, long value, boolean first) {
		if (!first) {
			builder.append(",");
		}
		builder.append("\"").append(name).append("\":").append(value);
	}

	private void appendNumberField(StringBuilder builder, String name, int value, boolean first) {
		if (!first) {
			builder.append(",");
		}
		builder.append("\"").append(name).append("\":").append(value);
	}

	private void appendDecimalField(StringBuilder builder, String name, BigDecimal value, boolean first) {
		if (!first) {
			builder.append(",");
		}
		builder.append("\"").append(name).append("\":").append(value.toPlainString());
	}

	private void appendStringArrayField(StringBuilder builder, String name, List<String> values, boolean first) {
		if (!first) {
			builder.append(",");
		}
		builder.append("\"").append(name).append("\":[");
		for (int index = 0; index < values.size(); index++) {
			if (index > 0) {
				builder.append(",");
			}
			builder.append("\"").append(escape(values.get(index))).append("\"");
		}
		builder.append("]");
	}

	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
