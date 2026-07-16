package com.sleekydz86.catalogflow.adapter.in.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.in.batch.config.CatalogMaintenanceJobConfig;
import com.sleekydz86.catalogflow.adapter.in.batch.config.ProductCsvImportJobConfig;
import com.sleekydz86.catalogflow.adapter.out.email.FakeEmailAdapter;
import com.sleekydz86.catalogflow.application.service.BatchJobLaunchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
@Sql(scripts = "/schema-catalog.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProductBatchJobsIntegrationTest {

	private static final UUID SUPPLIER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID CATEGORY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@Autowired
	private BatchJobLaunchService batchJobLaunchService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private FakeEmailAdapter fakeEmailAdapter;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() {
		fakeEmailAdapter.clear();
		jdbcTemplate.update("DELETE FROM product_images");
		jdbcTemplate.update("DELETE FROM outbox_events");
		jdbcTemplate.update("DELETE FROM batch_import_product_codes");
		jdbcTemplate.update("DELETE FROM products");
		jdbcTemplate.update("DELETE FROM suppliers");
		jdbcTemplate.update(
				"""
						INSERT INTO suppliers (
						  id, name, manufacturer, country_of_origin, classification_code, available, created_at, updated_at
						) VALUES (?, '테스트공급사', '제조사', 'KR', 'A', TRUE, ?, ?)
						""",
				SUPPLIER_ID,
				Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")),
				Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")));
	}

	@Test
	@DisplayName("CSV Import Job은 유효 행을 저장하고 검증 실패 행은 Skip 한다")
	void shouldImportValidRowsAndSkipInvalidRows() throws Exception {
		// given
		Path csv = tempDir.resolve("products-sample.csv");
		Files.copy(
				new ClassPathResource("csv/products-sample.csv").getInputStream(),
				csv,
				StandardCopyOption.REPLACE_EXISTING);

		// when
		JobExecution execution = batchJobLaunchService.launchCsvImport(csv.toAbsolutePath().toString());

		// then
		assertEquals(BatchStatus.COMPLETED, execution.getStatus());
		Integer productCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
		Integer codeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM batch_import_product_codes", Integer.class);
		Integer outboxCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM outbox_events WHERE event_type = 'ProductCreated'",
				Integer.class);
		assertEquals(3, productCount);
		assertEquals(3, codeCount);
		assertEquals(3, outboxCount);
		assertTrue(execution.getStepExecutions().iterator().next().getSkipCount() >= 2);
	}

	@Test
	@DisplayName("임시 이미지 정리 Job은 보존 기간이 지난 임시 이미지를 삭제한다")
	void shouldCleanupExpiredTemporaryImages() {
		// given
		UUID productId = UUID.randomUUID();
		jdbcTemplate.update(
				"""
						INSERT INTO products (
						  id, name, description, price_amount, price_currency, status, category_id, supplier_id,
						  ai_enrichment_status, version, published_at, deleted, created_at, updated_at
						) VALUES (?, '정리대상', '설명', 1000, 'KRW', 'DRAFT', ?, ?, 'NOT_REQUESTED', 0, NULL, FALSE, ?, ?)
						""",
				productId,
				CATEGORY_ID,
				SUPPLIER_ID,
				Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")),
				Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")));
		jdbcTemplate.update(
				"""
						INSERT INTO product_images (
						  id, product_id, image_id, storage_key, content_type, size_in_bytes, temporary, uploaded_at
						) VALUES (?, ?, 'img-1', 'temp/old.png', 'image/png', 10, TRUE, ?)
						""",
				UUID.randomUUID(),
				productId,
				Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")));

		// when
		JobExecution execution = batchJobLaunchService.launch(CatalogMaintenanceJobConfig.TEMP_IMAGE_CLEANUP_JOB);

		// then
		assertEquals(BatchStatus.COMPLETED, execution.getStatus());
		Integer remaining = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product_images", Integer.class);
		assertEquals(0, remaining);
	}

	@Test
	@DisplayName("일일 리포트 Job은 이메일을 발송하고 리포트 이력을 저장한다")
	void shouldSendDailyReportEmail() {
		// given
		// seed supplier already inserted

		// when
		JobExecution execution = batchJobLaunchService.launch(CatalogMaintenanceJobConfig.DAILY_REPORT_JOB);

		// then
		assertEquals(BatchStatus.COMPLETED, execution.getStatus());
		assertEquals(1, fakeEmailAdapter.getSentMails().size());
		assertTrue(fakeEmailAdapter.getSentMails().getFirst().subject().contains("일일 배치 리포트"));
		Integer reportCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM batch_job_reports", Integer.class);
		assertEquals(1, reportCount);
	}

	@Test
	@DisplayName("AI 실패 재처리 Job은 FAILED 상품을 REQUESTED로 되돌리고 이벤트를 기록한다")
	void shouldRetryFailedAiEnrichment() {
		// given
		UUID productId = UUID.randomUUID();
		jdbcTemplate.update(
				"""
						INSERT INTO products (
						  id, name, description, price_amount, price_currency, status, category_id, supplier_id,
						  ai_enrichment_status, version, published_at, deleted, created_at, updated_at
						) VALUES (?, 'AI실패상품', '설명', 3000, 'KRW', 'DRAFT', ?, ?, 'FAILED', 1, NULL, FALSE, ?, ?)
						""",
				productId,
				CATEGORY_ID,
				SUPPLIER_ID,
				Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")),
				Timestamp.from(Instant.parse("2026-01-01T00:00:00Z")));

		// when
		JobExecution execution = batchJobLaunchService.launch(CatalogMaintenanceJobConfig.AI_RETRY_JOB);

		// then
		assertEquals(BatchStatus.COMPLETED, execution.getStatus());
		String status = jdbcTemplate.queryForObject(
				"SELECT ai_enrichment_status FROM products WHERE id = ?",
				String.class,
				productId);
		assertEquals("REQUESTED", status);
		Integer outboxCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM outbox_events WHERE event_type = 'ProductEnrichmentRequested'",
				Integer.class);
		assertEquals(1, outboxCount);
	}

	@Test
	@DisplayName("CSV Import Job 이름은 알려진 배치 작업으로 식별된다")
	void shouldRecognizeKnownJobNames() {
		// given / when / then
		assertTrue(BatchJobLaunchService.isKnownJob(ProductCsvImportJobConfig.JOB_NAME));
		assertTrue(BatchJobLaunchService.isKnownJob(CatalogMaintenanceJobConfig.DAILY_REPORT_JOB));
	}
}
