package com.sleekydz86.catalogflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
@Sql(scripts = "/schema-catalog.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class CatalogBatchServiceApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@Test
	@DisplayName("배치 서비스 애플리케이션 컨텍스트가 정상적으로 기동된다")
	void contextLoads() {
		// given / when / then
	}
}
