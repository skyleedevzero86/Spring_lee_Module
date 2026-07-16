package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.time.Instant;
import java.util.List;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.OutboxEventEntity;
import com.sleekydz86.catalogflow.application.port.out.OutboxEventPort;
import com.sleekydz86.catalogflow.domain.event.DomainEvent;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventPersistenceAdapter implements OutboxEventPort {

	private static final String AGGREGATE_TYPE = "Product";

	private final OutboxEventJpaRepository outboxEventJpaRepository;

	public OutboxEventPersistenceAdapter(OutboxEventJpaRepository outboxEventJpaRepository) {
		this.outboxEventJpaRepository = outboxEventJpaRepository;
	}

	@Override
	public void saveAll(List<DomainEvent> events) {
		for (DomainEvent event : events) {
			OutboxEventEntity entity = OutboxEventEntity.createEmpty();
			entity.setId(event.eventId());
			entity.setAggregateId(event.aggregateId().value());
			entity.setAggregateType(AGGREGATE_TYPE);
			entity.setEventType(event.eventType());
			entity.setAggregateVersion(event.aggregateVersion());
			entity.setPayload(toPayload(event));
			entity.setCorrelationId(event.correlationId());
			entity.setCausationId(event.causationId());
			entity.setSchemaVersion(event.schemaVersion());
			entity.setPublished(false);
			entity.setCreatedAt(event.occurredAt());
			outboxEventJpaRepository.save(entity);
		}
	}

	private String toPayload(DomainEvent event) {
		return "{"
				+ "\"eventId\":\"" + event.eventId() + "\","
				+ "\"eventType\":\"" + escape(event.eventType()) + "\","
				+ "\"aggregateId\":\"" + event.aggregateId().value() + "\","
				+ "\"aggregateVersion\":" + event.aggregateVersion() + ","
				+ "\"occurredAt\":\"" + escape(instantToString(event.occurredAt())) + "\","
				+ "\"correlationId\":\"" + escape(event.correlationId()) + "\","
				+ "\"causationId\":\"" + escape(event.causationId()) + "\","
				+ "\"schemaVersion\":" + event.schemaVersion()
				+ "}";
	}

	private String instantToString(Instant instant) {
		return instant == null ? "" : instant.toString();
	}

	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
