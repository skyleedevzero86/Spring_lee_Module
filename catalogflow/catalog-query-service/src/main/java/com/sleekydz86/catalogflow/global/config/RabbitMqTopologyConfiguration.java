package com.sleekydz86.catalogflow.global.config;

import java.util.ArrayList;
import java.util.List;

import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.eventcontract.CatalogRoutingKeys;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
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
	DirectExchange catalogCommandsExchange(MessagingProperties messagingProperties) {
		return new DirectExchange(messagingProperties.getExchangeCommands(), true, false);
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
	Queue queryProductEventsQueue(MessagingProperties messagingProperties) {
		return QueueBuilder.durable(messagingProperties.getQueueQueryProductEvents())
				.withArgument("x-dead-letter-exchange", messagingProperties.getExchangeDeadLetter())
				.withArgument("x-dead-letter-routing-key", CatalogRoutingKeys.DEAD_LETTER)
				.build();
	}

	@Bean
	Declarables queryProductEventBindings(Queue queryProductEventsQueue, DirectExchange catalogEventsExchange) {
		List<Declarable> declarables = new ArrayList<>();
		for (String eventType : CatalogEventTypes.QUERY_PRODUCT_EVENT_TYPES) {
			declarables.add(BindingBuilder.bind(queryProductEventsQueue)
					.to(catalogEventsExchange)
					.with(CatalogRoutingKeys.resolve(eventType)));
		}
		return new Declarables(declarables);
	}

	@Bean
	Queue queryProductEventsRetryQueue(MessagingProperties messagingProperties) {
		return QueueBuilder.durable(messagingProperties.getQueueQueryProductEventsRetry())
				.withArgument("x-dead-letter-exchange", "")
				.withArgument("x-dead-letter-routing-key", messagingProperties.getQueueQueryProductEvents())
				.build();
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

	@Bean
	Queue enrichmentResultsQueue(MessagingProperties messagingProperties) {
		return QueueBuilder.durable(messagingProperties.getQueueEnrichmentResults())
				.withArgument("x-dead-letter-exchange", messagingProperties.getExchangeDeadLetter())
				.withArgument("x-dead-letter-routing-key", CatalogRoutingKeys.DEAD_LETTER)
				.build();
	}

	@Bean
	Declarables enrichmentResultBindings(
			Queue enrichmentResultsQueue,
			DirectExchange catalogEventsExchange) {
		List<Declarable> declarables = new ArrayList<>();
		for (String eventType : CatalogEventTypes.COMMAND_ENRICHMENT_RESULT_EVENT_TYPES) {
			declarables.add(BindingBuilder.bind(enrichmentResultsQueue)
					.to(catalogEventsExchange)
					.with(CatalogRoutingKeys.resolve(eventType)));
		}
		return new Declarables(declarables);
	}

	@Bean
	Queue batchRetryQueue(MessagingProperties messagingProperties) {
		return QueueBuilder.durable(messagingProperties.getQueueBatchRetry())
				.withArgument("x-dead-letter-exchange", messagingProperties.getExchangeDeadLetter())
				.withArgument("x-dead-letter-routing-key", CatalogRoutingKeys.DEAD_LETTER)
				.build();
	}

	@Bean
	Binding batchRetryBinding(Queue batchRetryQueue, DirectExchange catalogCommandsExchange) {
		return BindingBuilder.bind(batchRetryQueue)
				.to(catalogCommandsExchange)
				.with(CatalogRoutingKeys.BATCH_RETRY);
	}
}
