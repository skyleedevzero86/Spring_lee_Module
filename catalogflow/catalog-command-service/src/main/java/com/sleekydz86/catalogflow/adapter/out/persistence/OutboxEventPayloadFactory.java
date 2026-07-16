package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductEntity;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductKeywordEntity;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductTagEntity;
import com.sleekydz86.catalogflow.adapter.out.persistence.entity.SupplierEntity;
import com.sleekydz86.catalogflow.domain.event.DomainEvent;
import com.sleekydz86.catalogflow.domain.event.ProductCreated;
import com.sleekydz86.catalogflow.domain.event.ProductDiscontinued;
import com.sleekydz86.catalogflow.domain.event.ProductEnrichmentCompleted;
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

	public OutboxEventPayloadFactory(
			SupplierJpaRepository supplierJpaRepository,
			ProductJpaRepository productJpaRepository) {
		this.supplierJpaRepository = supplierJpaRepository;
		this.productJpaRepository = productJpaRepository;
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
		if (event instanceof ProductEnrichmentCompleted enrichmentCompleted) {
			appendProductEnrichmentCompleted(builder, enrichmentCompleted);
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

	private void appendProductEnrichmentCompleted(StringBuilder builder, ProductEnrichmentCompleted event) {
		ProductEntity product = productJpaRepository.findById(event.aggregateId().value())
				.orElseThrow(() -> new ApplicationException("AI 가공 완료 상품을 찾을 수 없습니다"));
		appendField(builder, "summary", truncate(product.getDescription(), 200), false);
		appendField(builder, "generatedDescription", product.getDescription(), false);
		appendField(builder, "modelName", event.modelName(), false);
		appendStringArrayField(builder, "keywords", readKeywords(product), false);
		appendStringArrayField(builder, "tags", readTags(product), false);
		appendField(builder, "status", product.getStatus(), false);
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
