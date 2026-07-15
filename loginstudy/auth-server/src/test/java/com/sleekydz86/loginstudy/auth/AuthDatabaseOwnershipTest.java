package com.sleekydz86.loginstudy.auth;

import static org.assertj.core.api.Assertions.assertThat;

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
	void flywayCreatesAuthOwnedSchemaMeta() {
		String serviceName = jdbcTemplate.queryForObject(
				"SELECT service_name FROM schema_meta WHERE service_name = ?",
				String.class,
				"auth-server");

		assertThat(serviceName).isEqualTo("auth-server");
	}
}
