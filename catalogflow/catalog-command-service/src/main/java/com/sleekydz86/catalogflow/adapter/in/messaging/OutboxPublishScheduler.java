package com.sleekydz86.catalogflow.adapter.in.messaging;

import com.sleekydz86.catalogflow.adapter.out.messaging.OutboxPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.outbox.publisher-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublishScheduler {

	private final OutboxPublisher outboxPublisher;

	public OutboxPublishScheduler(OutboxPublisher outboxPublisher) {
		this.outboxPublisher = outboxPublisher;
	}

	@Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:3000}")
	public void publish() {
		outboxPublisher.publishPendingEvents(100);
	}
}
