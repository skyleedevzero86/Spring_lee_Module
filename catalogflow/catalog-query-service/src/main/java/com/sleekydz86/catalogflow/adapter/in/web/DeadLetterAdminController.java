package com.sleekydz86.catalogflow.adapter.in.web;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sleekydz86.catalogflow.eventcontract.CatalogQueues;
import com.sleekydz86.catalogflow.eventcontract.MessagingHeaders;
import com.sleekydz86.catalogflow.global.config.MessagingProperties;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dead-letters")
public class DeadLetterAdminController {

	private final RabbitTemplate rabbitTemplate;
	private final MessagingProperties messagingProperties;

	public DeadLetterAdminController(RabbitTemplate rabbitTemplate, MessagingProperties messagingProperties) {
		this.rabbitTemplate = rabbitTemplate;
		this.messagingProperties = messagingProperties;
	}

	@GetMapping
	public Map<String, Object> listDeadLetters() {
		List<Map<String, Object>> messages = new ArrayList<>();
		Message message;
		while ((message = rabbitTemplate.receive(CatalogQueues.DEAD_LETTER, 100)) != null) {
			messages.add(Map.of(
					"messageId", message.getMessageProperties().getMessageId() == null
							? ""
							: message.getMessageProperties().getMessageId(),
					"routingKey", message.getMessageProperties().getReceivedRoutingKey() == null
							? ""
							: message.getMessageProperties().getReceivedRoutingKey(),
					"body", new String(message.getBody(), StandardCharsets.UTF_8)));
		}
		return Map.of(
				"queue", CatalogQueues.DEAD_LETTER,
				"count", messages.size(),
				"messages", messages);
	}

	@PostMapping("/requeue")
	public Map<String, Object> requeueDeadLetters() {
		int requeued = 0;
		Message message;
		while ((message = rabbitTemplate.receive(CatalogQueues.DEAD_LETTER, 100)) != null) {
			Object originalRoutingKey = message.getMessageProperties().getHeaders().get(MessagingHeaders.ORIGINAL_ROUTING_KEY);
			if (originalRoutingKey == null || originalRoutingKey.toString().isBlank()) {
				throw new ApplicationException("원본 라우팅 키가 없는 데드레터 메시지는 재처리할 수 없습니다");
			}
			rabbitTemplate.send(messagingProperties.getExchangeEvents(), originalRoutingKey.toString(), message);
			requeued++;
		}
		return Map.of("requeued", requeued);
	}
}
