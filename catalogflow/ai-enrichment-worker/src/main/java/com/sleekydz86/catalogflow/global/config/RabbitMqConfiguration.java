package com.sleekydz86.catalogflow.global.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.CatalogRoutingKeys;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {

	@Bean
	RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMandatory(true);
		rabbitTemplate.setReturnsCallback(returned -> {
			throw new ApplicationException("발행되지 않은 메시지가 반환되었습니다: " + returned.getReplyText());
		});
		rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
			if (!ack && correlationData != null) {
				correlationData.getFuture().completeExceptionally(
						new ApplicationException("메시지 발행 확인에 실패했습니다: " + cause));
			}
		});
		return rabbitTemplate;
	}

	@Bean
	SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			ConnectionFactory connectionFactory,
			MessagingProperties messagingProperties) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
		factory.setPrefetchCount(messagingProperties.getConsumerPrefetch());
		factory.setDefaultRequeueRejected(false);
		return factory;
	}

	@Bean
	DirectExchange catalogEventsExchange(MessagingProperties messagingProperties) {
		return new DirectExchange(messagingProperties.getExchangeEvents(), true, false);
	}

	@Bean
	DirectExchange catalogDeadLetterExchange(MessagingProperties messagingProperties) {
		return new DirectExchange(messagingProperties.getExchangeDeadLetter(), true, false);
	}

	@Bean
	Queue deadLetterQueue(MessagingProperties messagingProperties) {
		return QueueBuilder.durable(messagingProperties.getQueueDeadLetter()).build();
	}

	@Bean
	Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange catalogDeadLetterExchange) {
		return BindingBuilder.bind(deadLetterQueue)
				.to(catalogDeadLetterExchange)
				.with(CatalogRoutingKeys.DEAD_LETTER);
	}

	@Bean
	Queue aiEnrichmentRequestsQueue(MessagingProperties messagingProperties) {
		return QueueBuilder.durable(messagingProperties.getQueueAiEnrichmentRequests())
				.withArgument("x-dead-letter-exchange", messagingProperties.getExchangeDeadLetter())
				.withArgument("x-dead-letter-routing-key", CatalogRoutingKeys.DEAD_LETTER)
				.build();
	}

	@Bean
	Declarables aiEnrichmentRequestBindings(
			Queue aiEnrichmentRequestsQueue,
			DirectExchange catalogEventsExchange) {
		List<Declarable> declarables = new ArrayList<>();
		for (String eventType : CatalogEventTypes.AI_ENRICHMENT_EVENT_TYPES) {
			declarables.add(BindingBuilder.bind(aiEnrichmentRequestsQueue)
					.to(catalogEventsExchange)
					.with(CatalogRoutingKeys.resolve(eventType)));
		}
		return new Declarables(declarables);
	}
}
