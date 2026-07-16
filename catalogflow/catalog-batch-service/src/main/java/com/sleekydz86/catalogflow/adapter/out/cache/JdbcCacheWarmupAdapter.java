package com.sleekydz86.catalogflow.adapter.out.cache;

import com.sleekydz86.catalogflow.application.port.out.CacheWarmupPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcCacheWarmupAdapter implements CacheWarmupPort {

	private final JdbcTemplate jdbcTemplate;

	public JdbcCacheWarmupAdapter(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public int warmUp(int limit) {
		Integer count = jdbcTemplate.queryForObject(
				"""
						SELECT COUNT(*) FROM (
						  SELECT id FROM products
						  WHERE deleted = FALSE AND status IN ('PUBLISHED', 'READY')
						  ORDER BY updated_at DESC
						  LIMIT ?
						) warmed
						""",
				Integer.class,
				Math.max(limit, 0));
		return count == null ? 0 : count;
	}
}
