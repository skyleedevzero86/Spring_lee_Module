package com.sleekydz86.catalogflow.testsupport;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.sleekydz86.catalogflow.eventcontract.MessagingHeaders;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

public final class FailureScenarioMessages {

	private FailureScenarioMessages() {
	}

	public static Message productEventMessage(String body, String routingKey) {
		MessageProperties properties = new MessageProperties();
		properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
		properties.setMessageId(UUID.randomUUID().toString());
		properties.setReceivedRoutingKey(routingKey);
		properties.setHeader(MessagingHeaders.EVENT_TYPE, "ProductCreated");
		properties.setHeader(MessagingHeaders.CORRELATION_ID, "corr-failure-test");
		return new Message(body.getBytes(StandardCharsets.UTF_8), properties);
	}

	public static Message invalidJsonMessage(String routingKey) {
		return productEventMessage("{not-json", routingKey);
	}
}
