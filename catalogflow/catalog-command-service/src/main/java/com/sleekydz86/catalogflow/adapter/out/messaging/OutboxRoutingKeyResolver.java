package com.sleekydz86.catalogflow.adapter.out.messaging;

import com.sleekydz86.catalogflow.global.config.MessagingProperties;
import org.springframework.stereotype.Component;

@Component
public class OutboxRoutingKeyResolver {

	private final MessagingProperties messagingProperties;

	public OutboxRoutingKeyResolver(MessagingProperties messagingProperties) {
		this.messagingProperties = messagingProperties;
	}

	public String resolve(String eventType) {
		String normalized = eventType
				.replace("Product", "")
				.replaceAll("([a-z])([A-Z])", "$1.$2")
				.toLowerCase();
		return messagingProperties.getRoutingKeyPrefix() + "." + normalized + ".v1";
	}
}
