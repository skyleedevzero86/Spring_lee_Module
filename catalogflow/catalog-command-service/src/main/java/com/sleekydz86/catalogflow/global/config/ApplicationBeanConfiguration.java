package com.sleekydz86.catalogflow.global.config;

import java.time.Clock;
import java.util.concurrent.Executor;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class ApplicationBeanConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMandatory(true);
		return rabbitTemplate;
	}

	@Bean
	Executor outboxPublisherExecutor(TaskExecutor taskExecutor) {
		return taskExecutor;
	}
}
