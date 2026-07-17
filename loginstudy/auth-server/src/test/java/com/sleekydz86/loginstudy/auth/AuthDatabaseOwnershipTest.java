package com.sleekydz86.loginstudy.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AuthDatabaseOwnershipTest extends AuthServerIntegrationTestSupport {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("Flyway가 인증 서비스 소유 스키마 메타 정보를 생성한다")
	void flywayCreatesAuthOwnedSchemaMeta() {
		// when
		String serviceName = jdbcTemplate.queryForObject(
				"SELECT service_name FROM schema_meta WHERE service_name = ?",
				String.class,
				"auth-server");

		// then
		assertThat(serviceName).isEqualTo("auth-server");
	}
}
