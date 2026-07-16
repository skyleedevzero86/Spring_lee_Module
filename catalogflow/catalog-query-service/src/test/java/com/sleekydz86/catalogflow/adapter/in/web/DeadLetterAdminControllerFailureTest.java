package com.sleekydz86.catalogflow.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import com.sleekydz86.catalogflow.eventcontract.CatalogQueues;
import com.sleekydz86.catalogflow.global.config.MessagingProperties;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class DeadLetterAdminControllerFailureTest {

	@Test
	@DisplayName("원본 라우팅 키가 없는 DLQ 메시지는 재처리를 거부한다")
	void shouldRejectRequeueWithoutOriginalRoutingKey() {
		// given
		RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
		MessagingProperties properties = new MessagingProperties();
		properties.setExchangeEvents("catalog.events");
		DeadLetterAdminController controller = new DeadLetterAdminController(rabbitTemplate, properties);
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setMessageId("dlq-1");
		Message message = new Message("{}".getBytes(StandardCharsets.UTF_8), messageProperties);
		when(rabbitTemplate.receive(eq(CatalogQueues.DEAD_LETTER), anyLong())).thenReturn(message, (Message) null);

		// when / then
		ApplicationException exception = assertThrows(ApplicationException.class, controller::requeueDeadLetters);
		assertTrue(exception.getMessage().contains("원본 라우팅 키"));
	}
}
