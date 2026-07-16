package com.sleekydz86.catalogflow.adapter.in.batch.csv;

import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.batch.model.ProductImportItem;
import com.sleekydz86.catalogflow.domain.event.DomainEvent;
import com.sleekydz86.catalogflow.domain.event.ProductCreated;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.global.exception.TransientBatchException;
import com.sleekydz86.catalogflow.global.util.InstantSql;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProductCsvItemWriter implements ItemWriter<ProductImportItem> {

	private final JdbcTemplate jdbcTemplate;

	public ProductCsvItemWriter(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	@Transactional
	public void write(Chunk<? extends ProductImportItem> chunk) {
		for (ProductImportItem item : chunk) {
			try {
				saveProduct(item);
			}
			catch (Exception exception) {
				throw new TransientBatchException("상품 저장 중 일시적 오류가 발생했습니다", exception);
			}
		}
	}

	private void saveProduct(ProductImportItem item) {
		Product product = item.product();
		jdbcTemplate.update(
				"""
						INSERT INTO products (
						  id, name, description, price_amount, price_currency, status, category_id, supplier_id,
						  ai_enrichment_status, version, published_at, deleted, created_at, updated_at
						) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE, ?, ?)
						""",
				product.getId().value(),
				product.getName().value(),
				product.getDescription().value(),
				product.getPrice().amount(),
				product.getPrice().currency(),
				product.getStatus().name(),
				product.getCategoryId().value(),
				product.getSupplierId().value(),
				product.getAiEnrichmentStatus().name(),
				product.getVersion(),
				InstantSql.toTimestamp(product.getPublishedAt()),
				InstantSql.toTimestamp(product.getCreatedAt()),
				InstantSql.toTimestamp(product.getUpdatedAt()));

		jdbcTemplate.update(
				"""
						INSERT INTO batch_import_product_codes (product_code, product_id, imported_at)
						VALUES (?, ?, ?)
						""",
				item.productCode(),
				product.getId().value(),
				InstantSql.toTimestamp(product.getCreatedAt()));

		List<DomainEvent> events = product.pullDomainEvents();
		for (DomainEvent event : events) {
			if (event instanceof ProductCreated created) {
				saveOutbox(created, product);
			}
		}
	}

	private void saveOutbox(ProductCreated event, Product product) {
		String payload = """
				{"eventId":"%s","eventType":"%s","aggregateId":"%s","aggregateVersion":%d,"occurredAt":"%s","correlationId":"%s","causationId":"","schemaVersion":1,"name":"%s","description":"%s","priceAmount":%s,"priceCurrency":"%s","categoryId":"%s","supplierId":"%s","supplierName":"","status":"%s","createdAt":"%s","updatedAt":"%s"}
				""".formatted(
				event.eventId(),
				event.eventType(),
				event.aggregateId().value(),
				event.aggregateVersion(),
				event.occurredAt(),
				escape(event.correlationId()),
				escape(product.getName().value()),
				escape(product.getDescription().value()),
				product.getPrice().amount().toPlainString(),
				product.getPrice().currency(),
				product.getCategoryId().value(),
				product.getSupplierId().value(),
				product.getStatus().name(),
				product.getCreatedAt(),
				product.getUpdatedAt()).trim();

		jdbcTemplate.update(
				"""
						INSERT INTO outbox_events (
						  id, aggregate_id, aggregate_type, event_type, aggregate_version, payload,
						  correlation_id, causation_id, schema_version, published, published_at, created_at
						) VALUES (?, ?, 'Product', ?, ?, ?::jsonb, ?, '', ?, FALSE, NULL, ?)
						""",
				event.eventId() == null ? UUID.randomUUID() : event.eventId(),
				product.getId().value(),
				event.eventType(),
				event.aggregateVersion(),
				payload,
				event.correlationId(),
				event.schemaVersion(),
				InstantSql.toTimestamp(event.occurredAt()));
	}

	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
