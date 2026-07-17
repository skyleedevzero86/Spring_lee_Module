package com.sleekydz86.loginstudy.member.security;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LegacySensitiveDataMigrator implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;
	private final SensitiveDataCipher cipher;

	public LegacySensitiveDataMigrator(JdbcTemplate jdbcTemplate, SensitiveDataCipher cipher) {
		this.jdbcTemplate = jdbcTemplate;
		this.cipher = cipher;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		List<SensitiveRow> rows = jdbcTemplate.query(
				"SELECT id, email, display_name FROM member_profile",
				(resultSet, rowNumber) -> new SensitiveRow(
						resultSet.getLong("id"),
						resultSet.getString("email"),
						resultSet.getString("display_name")));

		for (SensitiveRow row : rows) {
			if (!cipher.isEncrypted(row.email()) || !cipher.isEncrypted(row.displayName())) {
				jdbcTemplate.update(
						"UPDATE member_profile SET email = ?, display_name = ? WHERE id = ?",
						cipher.encrypt(row.email()),
						cipher.encrypt(row.displayName()),
						row.id());
			}
		}
	}

	private record SensitiveRow(long id, String email, String displayName) {
	}
}
