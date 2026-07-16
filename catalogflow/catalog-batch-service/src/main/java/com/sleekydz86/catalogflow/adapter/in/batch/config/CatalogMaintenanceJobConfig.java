package com.sleekydz86.catalogflow.adapter.in.batch.config;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.port.out.CacheWarmupPort;
import com.sleekydz86.catalogflow.application.port.out.EmailPort;
import com.sleekydz86.catalogflow.eventcontract.CatalogEventTypes;
import com.sleekydz86.catalogflow.global.config.BatchProperties;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import com.sleekydz86.catalogflow.global.util.InstantSql;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CatalogMaintenanceJobConfig {

	public static final String READ_MODEL_REBUILD_JOB = "productReadModelRebuildJob";
	public static final String CACHE_WARMUP_JOB = "productCacheWarmupJob";
	public static final String AI_RETRY_JOB = "failedAiEnrichmentRetryJob";
	public static final String TEMP_IMAGE_CLEANUP_JOB = "temporaryImageCleanupJob";
	public static final String DAILY_REPORT_JOB = "dailyProcessingReportJob";

	@Bean
	Tasklet readModelRebuildTasklet(JdbcTemplate jdbcTemplate, Clock clock) {
		return (contribution, chunkContext) -> {
			List<Map<String, Object>> products = jdbcTemplate.queryForList(
					"""
							SELECT id, name, description, price_amount, price_currency, status, category_id, supplier_id, version, created_at, updated_at
							FROM products
							WHERE deleted = FALSE
							""");
			Instant now = clock.instant();
			for (Map<String, Object> product : products) {
				UUID productId = (UUID) product.get("id");
				UUID eventId = UUID.randomUUID();
				long version = ((Number) product.get("version")).longValue();
				String payload = """
						{"eventId":"%s","eventType":"%s","aggregateId":"%s","aggregateVersion":%d,"occurredAt":"%s","correlationId":"%s","causationId":"","schemaVersion":1,"name":"%s","description":"%s","categoryId":"%s","supplierId":"%s","supplierName":"","status":"%s","updatedAt":"%s"}
						""".formatted(
						eventId,
						CatalogEventTypes.PRODUCT_UPDATED,
						productId,
						version,
						now,
						CorrelationIdHolder.getOrGenerate(),
						escape(String.valueOf(product.get("name"))),
						escape(String.valueOf(product.get("description"))),
						product.get("category_id"),
						product.get("supplier_id"),
						product.get("status"),
						now).trim();
				jdbcTemplate.update(
						"""
								INSERT INTO outbox_events (
								  id, aggregate_id, aggregate_type, event_type, aggregate_version, payload,
								  correlation_id, causation_id, schema_version, published, published_at, created_at
								) VALUES (?, ?, 'Product', ?, ?, ?::jsonb, ?, '', 1, FALSE, NULL, ?)
								""",
						eventId,
						productId,
						CatalogEventTypes.PRODUCT_UPDATED,
						version,
						payload,
						CorrelationIdHolder.getOrGenerate(),
						InstantSql.toTimestamp(now));
			}
			contribution.incrementWriteCount(products.size());
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	Step readModelRebuildStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			Tasklet readModelRebuildTasklet) {
		return new StepBuilder("readModelRebuildStep", jobRepository)
				.tasklet(readModelRebuildTasklet, transactionManager)
				.build();
	}

	@Bean
	Job productReadModelRebuildJob(JobRepository jobRepository, Step readModelRebuildStep) {
		return new JobBuilder(READ_MODEL_REBUILD_JOB, jobRepository)
				.start(readModelRebuildStep)
				.build();
	}

	@Bean
	Tasklet cacheWarmupTasklet(CacheWarmupPort cacheWarmupPort, BatchProperties batchProperties) {
		return (contribution, chunkContext) -> {
			int warmed = cacheWarmupPort.warmUp(batchProperties.getCacheWarmupLimit());
			contribution.incrementWriteCount(warmed);
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	Step cacheWarmupStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			Tasklet cacheWarmupTasklet) {
		return new StepBuilder("cacheWarmupStep", jobRepository)
				.tasklet(cacheWarmupTasklet, transactionManager)
				.build();
	}

	@Bean
	Job productCacheWarmupJob(JobRepository jobRepository, Step cacheWarmupStep) {
		return new JobBuilder(CACHE_WARMUP_JOB, jobRepository)
				.start(cacheWarmupStep)
				.build();
	}

	@Bean
	Tasklet failedAiEnrichmentRetryTasklet(JdbcTemplate jdbcTemplate, Clock clock) {
		return (contribution, chunkContext) -> {
			List<Map<String, Object>> failedProducts = jdbcTemplate.queryForList(
					"""
							SELECT id, version FROM products
							WHERE deleted = FALSE AND ai_enrichment_status = 'FAILED'
							""");
			Instant now = clock.instant();
			for (Map<String, Object> product : failedProducts) {
				UUID productId = (UUID) product.get("id");
				long currentVersion = ((Number) product.get("version")).longValue();
				long nextVersion = currentVersion + 1;
				jdbcTemplate.update(
						"""
								UPDATE products
								SET ai_enrichment_status = 'REQUESTED',
								    status = 'ENRICHMENT_PENDING',
								    version = ?,
								    updated_at = ?
								WHERE id = ? AND version = ?
								""",
						nextVersion,
						InstantSql.toTimestamp(now),
						productId,
						currentVersion);
				UUID eventId = UUID.randomUUID();
				Map<String, Object> detail = jdbcTemplate.queryForMap(
						"""
								SELECT name, description, category_id, supplier_id, status
								FROM products WHERE id = ?
								""",
						productId);
				String payload = """
						{"eventId":"%s","eventType":"%s","aggregateId":"%s","aggregateVersion":%d,"occurredAt":"%s","correlationId":"%s","causationId":"","schemaVersion":1,"name":"%s","description":"%s","categoryId":"%s","supplierId":"%s","supplierName":"","status":"%s","updatedAt":"%s"}
						""".formatted(
						eventId,
						CatalogEventTypes.PRODUCT_ENRICHMENT_REQUESTED,
						productId,
						nextVersion,
						now,
						CorrelationIdHolder.getOrGenerate(),
						escape(String.valueOf(detail.get("name"))),
						escape(String.valueOf(detail.get("description"))),
						detail.get("category_id"),
						detail.get("supplier_id"),
						detail.get("status"),
						now).trim();
				jdbcTemplate.update(
						"""
								INSERT INTO outbox_events (
								  id, aggregate_id, aggregate_type, event_type, aggregate_version, payload,
								  correlation_id, causation_id, schema_version, published, published_at, created_at
								) VALUES (?, ?, 'Product', ?, ?, ?::jsonb, ?, '', 1, FALSE, NULL, ?)
								""",
						eventId,
						productId,
						CatalogEventTypes.PRODUCT_ENRICHMENT_REQUESTED,
						nextVersion,
						payload,
						CorrelationIdHolder.getOrGenerate(),
						InstantSql.toTimestamp(now));
			}
			contribution.incrementWriteCount(failedProducts.size());
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	Step failedAiEnrichmentRetryStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			Tasklet failedAiEnrichmentRetryTasklet) {
		return new StepBuilder("failedAiEnrichmentRetryStep", jobRepository)
				.tasklet(failedAiEnrichmentRetryTasklet, transactionManager)
				.build();
	}

	@Bean
	Job failedAiEnrichmentRetryJob(JobRepository jobRepository, Step failedAiEnrichmentRetryStep) {
		return new JobBuilder(AI_RETRY_JOB, jobRepository)
				.start(failedAiEnrichmentRetryStep)
				.build();
	}

	@Bean
	Tasklet temporaryImageCleanupTasklet(JdbcTemplate jdbcTemplate, BatchProperties batchProperties, Clock clock) {
		return (contribution, chunkContext) -> {
			Instant threshold = clock.instant().minusSeconds(batchProperties.getTempImageRetentionDays() * 24L * 3600L);
			int deleted = jdbcTemplate.update(
					"""
							DELETE FROM product_images
							WHERE temporary = TRUE AND uploaded_at < ?
							""",
					InstantSql.toTimestamp(threshold));
			contribution.incrementWriteCount(deleted);
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	Step temporaryImageCleanupStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			Tasklet temporaryImageCleanupTasklet) {
		return new StepBuilder("temporaryImageCleanupStep", jobRepository)
				.tasklet(temporaryImageCleanupTasklet, transactionManager)
				.build();
	}

	@Bean
	Job temporaryImageCleanupJob(JobRepository jobRepository, Step temporaryImageCleanupStep) {
		return new JobBuilder(TEMP_IMAGE_CLEANUP_JOB, jobRepository)
				.start(temporaryImageCleanupStep)
				.build();
	}

	@Bean
	Tasklet dailyProcessingReportTasklet(
			JdbcTemplate jdbcTemplate,
			EmailPort emailPort,
			BatchProperties batchProperties,
			Clock clock) {
		return (contribution, chunkContext) -> {
			Long productCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM products WHERE deleted = FALSE",
					Long.class);
			Long failedAiCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM products WHERE ai_enrichment_status = 'FAILED'",
					Long.class);
			Long tempImageCount = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM product_images WHERE temporary = TRUE",
					Long.class);
			String html = """
					<html><body>
					<h2>CatalogFlow 일일 배치 리포트</h2>
					<ul>
					<li>전체 상품 수: %d</li>
					<li>AI 실패 상품 수: %d</li>
					<li>임시 이미지 수: %d</li>
					<li>생성 시각: %s</li>
					</ul>
					</body></html>
					""".formatted(
					productCount == null ? 0 : productCount,
					failedAiCount == null ? 0 : failedAiCount,
					tempImageCount == null ? 0 : tempImageCount,
					clock.instant());
			emailPort.send(batchProperties.getReportMailTo(), "CatalogFlow 일일 배치 리포트", html);
			jdbcTemplate.update(
					"""
							INSERT INTO batch_job_reports (
							  id, job_name, execution_id, total_count, success_count, skip_count, fail_count, message, created_at
							) VALUES (?, ?, NULL, ?, ?, 0, ?, ?, ?)
							""",
					UUID.randomUUID(),
					DAILY_REPORT_JOB,
					productCount == null ? 0 : productCount,
					productCount == null ? 0 : productCount,
					failedAiCount == null ? 0 : failedAiCount,
					"일일 리포트 발송 완료",
					InstantSql.toTimestamp(clock.instant()));
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	Step dailyProcessingReportStep(
			JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			Tasklet dailyProcessingReportTasklet) {
		return new StepBuilder("dailyProcessingReportStep", jobRepository)
				.tasklet(dailyProcessingReportTasklet, transactionManager)
				.build();
	}

	@Bean
	Job dailyProcessingReportJob(JobRepository jobRepository, Step dailyProcessingReportStep) {
		return new JobBuilder(DAILY_REPORT_JOB, jobRepository)
				.start(dailyProcessingReportStep)
				.build();
	}

	private static String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
