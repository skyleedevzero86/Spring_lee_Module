package com.sleekydz86.catalogflow.eventcontract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class IntegrationEventPayloads {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

	private IntegrationEventPayloads() {
	}

	public static ProductCreatedData readProductCreated(String json) {
		JsonNode root = readRoot(json);
		return new ProductCreatedData(
				requiredText(root, "name"),
				requiredText(root, "description"),
				requiredDecimal(root, "priceAmount"),
				requiredText(root, "priceCurrency"),
				UUID.fromString(requiredText(root, "categoryId")),
				UUID.fromString(requiredText(root, "supplierId")),
				optionalText(root, "supplierName"),
				requiredText(root, "status"),
				Instant.parse(requiredText(root, "createdAt")),
				Instant.parse(requiredText(root, "updatedAt")));
	}

	public static ProductUpdatedData readProductUpdated(String json) {
		JsonNode root = readRoot(json);
		return new ProductUpdatedData(
				requiredText(root, "name"),
				requiredText(root, "description"),
				UUID.fromString(requiredText(root, "categoryId")),
				UUID.fromString(requiredText(root, "supplierId")),
				optionalText(root, "supplierName"),
				requiredText(root, "status"),
				Instant.parse(requiredText(root, "updatedAt")));
	}

	public static ProductPriceChangedData readProductPriceChanged(String json) {
		JsonNode root = readRoot(json);
		return new ProductPriceChangedData(
				requiredDecimal(root, "priceAmount"),
				requiredText(root, "priceCurrency"),
				Instant.parse(requiredText(root, "updatedAt")));
	}

	public static ProductImageUploadedData readProductImageUploaded(String json) {
		JsonNode root = readRoot(json);
		return new ProductImageUploadedData(
				requiredText(root, "imageId"),
				requiredText(root, "storageKey"),
				requiredText(root, "contentType"),
				Instant.parse(requiredText(root, "updatedAt")));
	}

	public static ProductEnrichmentCompletedData readProductEnrichmentCompleted(String json) {
		JsonNode root = readRoot(json);
		return new ProductEnrichmentCompletedData(
				optionalText(root, "summary"),
				optionalText(root, "generatedDescription"),
				optionalText(root, "modelName"),
				readStringList(root, "keywords"),
				readStringList(root, "tags"),
				requiredText(root, "status"),
				Instant.parse(requiredText(root, "updatedAt")));
	}

	public static ProductPublishedData readProductPublished(String json) {
		JsonNode root = readRoot(json);
		return new ProductPublishedData(
				requiredText(root, "status"),
				Instant.parse(requiredText(root, "publishedAt")),
				Instant.parse(requiredText(root, "updatedAt")));
	}

	public static ProductStatusChangedData readProductStatusChanged(String json) {
		JsonNode root = readRoot(json);
		return new ProductStatusChangedData(
				requiredText(root, "status"),
				Instant.parse(requiredText(root, "updatedAt")));
	}

	private static JsonNode readRoot(String json) {
		try {
			return OBJECT_MAPPER.readTree(json);
		}
		catch (Exception exception) {
			throw new IntegrationEventParseException("통합 이벤트 JSON 파싱에 실패했습니다", exception);
		}
	}

	private static String requiredText(JsonNode root, String fieldName) {
		JsonNode node = root.get(fieldName);
		if (node == null || node.isNull() || node.asText().isBlank()) {
			throw new IntegrationEventParseException(fieldName + " 필드가 필요합니다");
		}
		return node.asText();
	}

	private static String optionalText(JsonNode root, String fieldName) {
		JsonNode node = root.get(fieldName);
		if (node == null || node.isNull()) {
			return "";
		}
		return node.asText();
	}

	private static BigDecimal requiredDecimal(JsonNode root, String fieldName) {
		JsonNode node = root.get(fieldName);
		if (node == null || node.isNull()) {
			throw new IntegrationEventParseException(fieldName + " 필드가 필요합니다");
		}
		return node.decimalValue();
	}

	private static List<String> readStringList(JsonNode root, String fieldName) {
		JsonNode node = root.get(fieldName);
		if (node == null || node.isNull() || !node.isArray()) {
			return List.of();
		}
		List<String> values = new ArrayList<>();
		node.forEach(item -> values.add(item.asText()));
		return List.copyOf(values);
	}

	public record ProductCreatedData(
			String name,
			String description,
			BigDecimal priceAmount,
			String priceCurrency,
			UUID categoryId,
			UUID supplierId,
			String supplierName,
			String status,
			Instant createdAt,
			Instant updatedAt) {
	}

	public record ProductUpdatedData(
			String name,
			String description,
			UUID categoryId,
			UUID supplierId,
			String supplierName,
			String status,
			Instant updatedAt) {
	}

	public record ProductPriceChangedData(
			BigDecimal priceAmount,
			String priceCurrency,
			Instant updatedAt) {
	}

	public record ProductImageUploadedData(
			String imageId,
			String storageKey,
			String contentType,
			Instant updatedAt) {
	}

	public record ProductEnrichmentCompletedData(
			String summary,
			String generatedDescription,
			String modelName,
			List<String> keywords,
			List<String> tags,
			String status,
			Instant updatedAt) {
	}

	public record ProductPublishedData(
			String status,
			Instant publishedAt,
			Instant updatedAt) {
	}

	public record ProductStatusChangedData(
			String status,
			Instant updatedAt) {
	}
}
