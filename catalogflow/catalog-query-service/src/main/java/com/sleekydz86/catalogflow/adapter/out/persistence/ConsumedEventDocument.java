package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "consumed_events")
@CompoundIndex(name = "idx_consumed_events_event_consumer", def = "{'eventId': 1, 'consumerName': 1}", unique = true)
public class ConsumedEventDocument {

	@Id
	private String id;
	private String eventId;
	private String consumerName;
	private Instant consumedAt;

	public static ConsumedEventDocument create(UUID eventId, String consumerName, Instant consumedAt) {
		ConsumedEventDocument document = new ConsumedEventDocument();
		document.id = eventId + ":" + consumerName;
		document.eventId = eventId.toString();
		document.consumerName = consumerName;
		document.consumedAt = consumedAt;
		return document;
	}

	public String getId() {
		return id;
	}

	public String getEventId() {
		return eventId;
	}

	public String getConsumerName() {
		return consumerName;
	}

	public Instant getConsumedAt() {
		return consumedAt;
	}
}
