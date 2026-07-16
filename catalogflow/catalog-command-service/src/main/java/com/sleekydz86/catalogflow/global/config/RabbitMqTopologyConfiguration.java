package com.sleekydz86.catalogflow.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqTopologyConfiguration {

	@Bean
	DirectExchange catalogEventsExchange(MessagingProperties messagingProperties) {
		return new DirectExchange(messagingProperties.getExchangeEvents(), true, false);
	}

	@Bean
	DirectExchange catalogDeadLetterExchange(MessagingProperties messagingProperties) {
		return new DirectExchange(messagingProperties.getExchangeDeadLetter(), true, false);
	}

	@Bean
	Queue enrichmentResultsQueue(MessagingProperties messagingProperties) {
		return QueueBuilder.durable(messagingProperties.getQueueEnrichmentResults())
				.withArgument("x-dead-letter-exchange", messagingProperties.getExchangeDeadLetter())
				.withArgument("x-message-ttl", 60000)
				.build();
	}

	@Bean
	Binding enrichmentResultsBinding(
			Queue enrichmentResultsQueue,
			DirectExchange catalogEventsExchange,
			MessagingProperties messagingProperties) {
		return BindingBuilder.bind(enrichmentResultsQueue)
				.to(catalogEventsExchange)
				.with(messagingProperties.getRoutingKeyPrefix() + ".enrichment.completed.v1");
	}
}
