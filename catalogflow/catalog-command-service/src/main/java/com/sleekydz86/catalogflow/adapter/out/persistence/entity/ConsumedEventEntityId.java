package com.sleekydz86.catalogflow.adapter.out.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ConsumedEventEntityId implements Serializable {

	private UUID eventId;
	private String consumerName;

	public ConsumedEventEntityId() {
	}

	public ConsumedEventEntityId(UUID eventId, String consumerName) {
		this.eventId = eventId;
		this.consumerName = consumerName;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ConsumedEventEntityId that)) {
			return false;
		}
		return Objects.equals(eventId, that.eventId) && Objects.equals(consumerName, that.consumerName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(eventId, consumerName);
	}
}
