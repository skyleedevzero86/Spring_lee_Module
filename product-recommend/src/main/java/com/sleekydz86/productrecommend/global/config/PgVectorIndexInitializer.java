package com.sleekydz86.productrecommend.global.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.vector", name = "mode", havingValue = "pgvector")
public class PgVectorIndexInitializer implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	public PgVectorIndexInitializer(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
		jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");
		jdbcTemplate.execute("""
			CREATE INDEX IF NOT EXISTS products_embedding_hnsw_cosine_idx
			ON products
			USING hnsw (embedding vector_cosine_ops)
			""");
		jdbcTemplate.execute("""
			CREATE INDEX IF NOT EXISTS products_name_trgm_idx
			ON products
			USING gin (name gin_trgm_ops)
			""");
	}
}
