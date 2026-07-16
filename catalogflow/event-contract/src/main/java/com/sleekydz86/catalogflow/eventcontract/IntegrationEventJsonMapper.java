package com.sleekydz86.catalogflow.eventcontract;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class IntegrationEventJsonMapper {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

	private IntegrationEventJsonMapper() {
	}

	public static IntegrationEventEnvelope read(String json) {
		try {
			JsonNode root = OBJECT_MAPPER.readTree(json);
			UUID eventId = UUID.fromString(requiredText(root, "eventId"));
			String eventType = requiredText(root, "eventType");
			UUID aggregateId = UUID.fromString(requiredText(root, "aggregateId"));
			long aggregateVersion = root.path("aggregateVersion").asLong(-1);
			if (aggregateVersion < 0) {
				throw new IntegrationEventParseException("aggregateVersion 값이 유효하지 않습니다");
			}
			String occurredAtText = requiredText(root, "occurredAt");
			Instant occurredAt = Instant.parse(occurredAtText);
			String correlationId = optionalText(root, "correlationId");
			String causationId = optionalText(root, "causationId");
			int schemaVersion = root.path("schemaVersion").asInt(1);
			return new IntegrationEventEnvelope(
					eventId,
					eventType,
					aggregateId,
					aggregateVersion,
					occurredAt,
					correlationId,
					causationId,
					schemaVersion,
					json);
		}
		catch (IntegrationEventParseException exception) {
			throw exception;
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
}
