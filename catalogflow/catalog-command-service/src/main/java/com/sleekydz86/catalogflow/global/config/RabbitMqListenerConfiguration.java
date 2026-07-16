package com.sleekydz86.catalogflow.global.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqListenerConfiguration {

	@Bean
	@ConditionalOnProperty(name = "app.messaging.consumer-enabled", havingValue = "true", matchIfMissing = true)
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
}
