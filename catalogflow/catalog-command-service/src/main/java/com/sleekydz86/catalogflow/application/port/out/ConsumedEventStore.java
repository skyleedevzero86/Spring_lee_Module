package com.sleekydz86.catalogflow.application.port.out;

import java.util.UUID;

public interface ConsumedEventStore {

	boolean isConsumed(UUID eventId, String consumerName);

	void markConsumed(UUID eventId, String consumerName);
}
