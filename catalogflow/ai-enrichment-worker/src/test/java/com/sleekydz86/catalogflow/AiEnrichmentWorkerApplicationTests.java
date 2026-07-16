package com.sleekydz86.catalogflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;

@SpringBootTest
@Testcontainers
class AiEnrichmentWorkerApplicationTests {

	@Container
	@ServiceConnection
	static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-management-alpine");

	@Test
	void contextLoads() {
	}
}
