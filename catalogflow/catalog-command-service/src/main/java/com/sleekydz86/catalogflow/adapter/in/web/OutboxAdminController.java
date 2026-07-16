package com.sleekydz86.catalogflow.adapter.in.web;

import com.sleekydz86.catalogflow.adapter.out.messaging.OutboxPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/outbox")
public class OutboxAdminController {

	private final OutboxPublisher outboxPublisher;

	public OutboxAdminController(OutboxPublisher outboxPublisher) {
		this.outboxPublisher = outboxPublisher;
	}

	@PostMapping("/publish")
	public PublishOutboxResponse publish() {
		int count = outboxPublisher.publishPendingEvents(100);
		return new PublishOutboxResponse(count);
	}

	public record PublishOutboxResponse(int publishedCount) {
	}
}
