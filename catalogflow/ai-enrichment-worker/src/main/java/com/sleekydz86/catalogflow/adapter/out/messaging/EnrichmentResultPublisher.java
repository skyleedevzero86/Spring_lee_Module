package com.sleekydz86.catalogflow.adapter.out.messaging;

import static com.sleekydz86.catalogflow.global.config.RabbitMqPublisherConfiguration.waitForPublisherConfirm;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.CatalogRoutingKeys;
import com.sleekydz86.catalogflow.eventcontract.MessagingHeaders;
import com.sleekydz86.catalogflow.global.config.MessagingProperties;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import com.sleekydz86.catalogflow.global.util.TraceIdHolder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EnrichmentResultPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final MessagingProperties messagingProperties;

	public EnrichmentResultPublisher(RabbitTemplate rabbitTemplate, MessagingProperties messagingProperties) {
		this.rabbitTemplate = rabbitTemplate;
		this.messagingProperties = messagingProperties;
	}

	public void publishCompleted(
			UUID productId,
			long aggregateVersion,
			String correlationId,
			String causationId,
			AiEnrichmentPort.EnrichmentResult result,
			Instant occurredAt) {
		UUID eventId = UUID.randomUUID();
		String payload = """
				{"eventId":"%s","eventType":"%s","aggregateId":"%s","aggregateVersion":%d,"occurredAt":"%s","correlationId":"%s","causationId":"%s","schemaVersion":1,"summary":"%s","generatedDescription":"%s","modelName":"%s","keywords":%s,"tags":%s,"recommendedCategory":"%s","warnings":"%s","requiresHumanReview":%s,"confidence":%s,"promptVersion":"%s","status":"REVIEW_REQUIRED","updatedAt":"%s"}
				""".formatted(
				eventId,
				CatalogEventTypes.PRODUCT_ENRICHMENT_COMPLETED,
				productId,
				aggregateVersion,
				occurredAt,
				escape(correlationId),
				escape(causationId),
				escape(result.summary()),
				escape(result.generatedDescription()),
				escape(result.modelName()),
				toJsonArray(result.keywords()),
				toJsonArray(result.tags()),
				escape(result.recommendedCategory()),
				escape(result.warnings()),
				result.requiresHumanReview(),
				result.confidence(),
				escape(result.promptVersion()),
				occurredAt).trim();
		send(CatalogEventTypes.PRODUCT_ENRICHMENT_COMPLETED, productId, correlationId, eventId, payload);
	}

	public void publishFailed(
			UUID productId,
			long aggregateVersion,
			String correlationId,
			String causationId,
			String reason,
			Instant occurredAt) {
		UUID eventId = UUID.randomUUID();
		String payload = """
				{"eventId":"%s","eventType":"%s","aggregateId":"%s","aggregateVersion":%d,"occurredAt":"%s","correlationId":"%s","causationId":"%s","schemaVersion":1,"reason":"%s","status":"DRAFT","updatedAt":"%s"}
				""".formatted(
				eventId,
				CatalogEventTypes.PRODUCT_ENRICHMENT_FAILED,
				productId,
				aggregateVersion,
				occurredAt,
				escape(correlationId),
				escape(causationId),
				escape(reason),
				occurredAt).trim();
		send(CatalogEventTypes.PRODUCT_ENRICHMENT_FAILED, productId, correlationId, eventId, payload);
	}

	private void send(String eventType, UUID productId, String correlationId, UUID eventId, String payload) {
		try {
			MessageProperties properties = new MessageProperties();
			properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
			properties.setContentEncoding(StandardCharsets.UTF_8.name());
			properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
			properties.setMessageId(eventId.toString());
			properties.setHeader(MessagingHeaders.EVENT_TYPE, eventType);
			properties.setHeader(MessagingHeaders.AGGREGATE_ID, productId.toString());
			properties.setHeader(
					MessagingHeaders.CORRELATION_ID,
					correlationId == null || correlationId.isBlank()
							? CorrelationIdHolder.getOrGenerate()
							: correlationId);
			properties.setHeader(MessagingHeaders.TRACE_ID, TraceIdHolder.getOrGenerate());
			Message message = new Message(payload.getBytes(StandardCharsets.UTF_8), properties);
			CorrelationData correlationData = new CorrelationData(eventId.toString());
			rabbitTemplate.send(
					messagingProperties.getExchangeEvents(),
					CatalogRoutingKeys.resolve(eventType),
					message,
					correlationData);
			waitForPublisherConfirm(correlationData, messagingProperties.getPublisherConfirmTimeoutMs());
		}
		catch (ApplicationException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new ApplicationException("AI 가공 결과 이벤트 발행에 실패했습니다", exception);
		}
	}

	private String toJsonArray(List<String> values) {
		return values.stream()
				.map(value -> "\"" + escape(value) + "\"")
				.collect(Collectors.joining(",", "[", "]"));
	}

	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
