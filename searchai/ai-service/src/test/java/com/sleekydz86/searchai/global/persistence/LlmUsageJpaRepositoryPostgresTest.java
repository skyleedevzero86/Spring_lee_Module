package com.sleekydz86.searchai.global.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LlmUsageJpaRepositoryPostgresTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
		.withDatabaseName("searchai")
		.withUsername("searchai")
		.withPassword("searchai");

	@DynamicPropertySource
	static void datasourceProps(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
	}

	@Autowired
	LlmUsageJpaRepository repository;

	@Test
	void persistsUsageRow() {
		LlmUsageJpaEntity entity = new LlmUsageJpaEntity();
		entity.setRequestId("req-1");
		entity.setUsername("user");
		entity.setRoutingType("JEV");
		entity.setModel("gpt-5.6-sol");
		entity.setSelectedModel("gpt-5.6-sol");
		entity.setActualModel("gpt-5.6-sol");
		entity.setTier("sol");
		entity.setFallbackOccurred(false);
		entity.setRequestType("GENERAL");
		entity.setPromptTokens(10);
		entity.setCompletionTokens(20);
		entity.setTotalTokens(30);
		entity.setEstimatedCost(0.01);
		entity.setLatencyMs(12);
		entity.setSuccess(true);
		entity.setCached(false);
		entity.setCreatedAt(Instant.now());

		LlmUsageJpaEntity saved = repository.save(entity);
		assertThat(saved.getId()).isNotNull();
		assertThat(repository.sumTokensSince("user", Instant.EPOCH)).isEqualTo(30);
	}
}
