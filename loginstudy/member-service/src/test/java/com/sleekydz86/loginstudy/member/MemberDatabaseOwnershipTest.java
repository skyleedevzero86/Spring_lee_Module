package com.sleekydz86.loginstudy.member;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MemberDatabaseOwnershipTest extends MemberRedisTestSupport {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("Flyway가 회원 서비스 소유 스키마와 테이블을 생성한다")
	void flywayCreatesMemberOwnedSchemaAndTables() {
		// when
		String serviceName = jdbcTemplate.queryForObject(
				"SELECT service_name FROM schema_meta WHERE service_name = ?",
				String.class,
				"member-service");
		// then
		assertThat(serviceName).isEqualTo("member-service");

		Integer tables = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*)
				FROM information_schema.tables
				WHERE table_schema = 'public'
				  AND table_name IN ('member_profile', 'member_address', 'member_preferences', 'member_status_history')
				""",
				Integer.class);
		assertThat(tables).isEqualTo(4);
	}
}
