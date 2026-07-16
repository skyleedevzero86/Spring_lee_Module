package com.sleekydz86.catalogflow.adapter.in.messaging;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.sleekydz86.catalogflow.eventcontract.CatalogRoutingKeys;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventParseException;
import com.sleekydz86.catalogflow.global.config.MessagingProperties;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import com.sleekydz86.catalogflow.testsupport.FailureScenarioMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class MessagingRetryHandlerFailureTest {

	private RabbitTemplate rabbitTemplate;
	private MessagingProperties messagingProperties;
	private MessagingRetryHandler handler;

	@BeforeEach
	void setUp() {
		rabbitTemplate = mock(RabbitTemplate.class);
		messagingProperties = new MessagingProperties();
		messagingProperties.setExchangeDeadLetter("catalog.dlx");
		messagingProperties.setQueueQueryProductEventsRetry("catalog-query.product-events.retry");
		messagingProperties.setRetryMaxAttempts(3);
		messagingProperties.setRetryInitialDelayMs(1000);
		messagingProperties.setRetryMultiplier(2.0);
		handler = new MessagingRetryHandler(rabbitTemplate, messagingProperties);
	}

	@Test
	@DisplayName("파싱 실패 메시지는 즉시 데드레터 큐로 이동한다")
	void shouldSendParseFailureToDeadLetter() {
		// given
		Message message = FailureScenarioMessages.invalidJsonMessage("catalog.product.created");

		// when
		handler.handleFailure(message, 0, new IntegrationEventParseException("잘못된 JSON"));

		// then
		verify(rabbitTemplate).send(eq("catalog.dlx"), eq(CatalogRoutingKeys.DEAD_LETTER), any(Message.class));
	}

	@Test
	@DisplayName("일시 장애는 재시도 큐로 보내고 지연을 설정한다")
	void shouldSendTransientFailureToRetryQueue() {
		// given
		Message message = FailureScenarioMessages.productEventMessage("{}", "catalog.product.created");

		// when
		handler.handleFailure(message, 0, new RuntimeException("일시 장애"));

		// then
		verify(rabbitTemplate).send(eq(""), eq("catalog-query.product-events.retry"), any(Message.class));
	}

	@Test
	@DisplayName("재시도 한도를 초과하면 데드레터 큐로 이동한다")
	void shouldSendToDeadLetterWhenRetryExhausted() {
		// given
		Message message = FailureScenarioMessages.productEventMessage("{}", "catalog.product.created");

		// when
		handler.handleFailure(message, 2, new RuntimeException("반복 장애"));

		// then
		verify(rabbitTemplate).send(eq("catalog.dlx"), eq(CatalogRoutingKeys.DEAD_LETTER), any(Message.class));
	}

	@Test
	@DisplayName("데드레터 발행 실패 시 한국어 예외를 던진다")
	void shouldThrowKoreanExceptionWhenDeadLetterPublishFails() {
		// given
		Message message = FailureScenarioMessages.invalidJsonMessage("catalog.product.created");
		doThrow(new RuntimeException("broker down"))
				.when(rabbitTemplate)
				.send(eq("catalog.dlx"), eq(CatalogRoutingKeys.DEAD_LETTER), any(Message.class));

		// when / then
		ApplicationException exception = assertThrows(
				ApplicationException.class,
				() -> handler.handleFailure(message, 0, new IntegrationEventParseException("잘못된 JSON")));
		org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("데드레터"));
	}
}
