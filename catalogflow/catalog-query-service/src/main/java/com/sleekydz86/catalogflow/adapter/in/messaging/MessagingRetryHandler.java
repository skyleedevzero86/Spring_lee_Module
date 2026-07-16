package com.sleekydz86.catalogflow.adapter.in.messaging;

import com.sleekydz86.catalogflow.eventcontract.CatalogRoutingKeys;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventParseException;
import com.sleekydz86.catalogflow.eventcontract.MessagingHeaders;
import com.sleekydz86.catalogflow.global.config.MessagingProperties;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessagingRetryHandler {

	private final RabbitTemplate rabbitTemplate;
	private final MessagingProperties messagingProperties;

	public MessagingRetryHandler(RabbitTemplate rabbitTemplate, MessagingProperties messagingProperties) {
		this.rabbitTemplate = rabbitTemplate;
		this.messagingProperties = messagingProperties;
	}

	public void handleFailure(Message message, int currentRetryCount, Exception exception) {
		if (exception instanceof IntegrationEventParseException) {
			sendToDeadLetter(message);
			return;
		}
		int nextRetryCount = currentRetryCount + 1;
		if (nextRetryCount >= messagingProperties.getRetryMaxAttempts()) {
			sendToDeadLetter(message);
			return;
		}
		long delayMs = calculateDelayMs(nextRetryCount);
		sendToRetryQueue(message, nextRetryCount, delayMs);
	}

	private long calculateDelayMs(int retryCount) {
		double multiplier = Math.pow(messagingProperties.getRetryMultiplier(), retryCount - 1);
		return Math.round(messagingProperties.getRetryInitialDelayMs() * multiplier);
	}

	private void sendToRetryQueue(Message message, int retryCount, long delayMs) {
		try {
			MessageProperties properties = copyProperties(message.getMessageProperties());
			properties.setHeader(MessagingHeaders.RETRY_COUNT, retryCount);
			properties.setExpiration(String.valueOf(delayMs));
			Message retryMessage = new Message(message.getBody(), properties);
			rabbitTemplate.send("", messagingProperties.getQueueQueryProductEventsRetry(), retryMessage);
		}
		catch (Exception exception) {
			throw new ApplicationException("재시도 큐 발행에 실패했습니다", exception);
		}
	}

	private void sendToDeadLetter(Message message) {
		try {
			MessageProperties properties = copyProperties(message.getMessageProperties());
			Message deadLetterMessage = new Message(message.getBody(), properties);
			rabbitTemplate.send(
					messagingProperties.getExchangeDeadLetter(),
					CatalogRoutingKeys.DEAD_LETTER,
					deadLetterMessage);
		}
		catch (Exception exception) {
			throw new ApplicationException("데드레터 큐 발행에 실패했습니다", exception);
		}
	}

	private MessageProperties copyProperties(MessageProperties source) {
		MessageProperties properties = new MessageProperties();
		properties.setContentType(source.getContentType());
		properties.setContentEncoding(source.getContentEncoding());
		properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
		properties.setMessageId(source.getMessageId());
		properties.setCorrelationId(source.getCorrelationId());
		source.getHeaders().forEach(properties::setHeader);
		if (!properties.getHeaders().containsKey(MessagingHeaders.ORIGINAL_ROUTING_KEY)) {
			properties.setHeader(MessagingHeaders.ORIGINAL_ROUTING_KEY, source.getReceivedRoutingKey());
		}
		return properties;
	}
}
