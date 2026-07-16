package com.sleekydz86.catalogflow.adapter.in.batch.csv;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.batch.model.ProductImportItem;
import com.sleekydz86.catalogflow.domain.model.CategoryId;
import com.sleekydz86.catalogflow.domain.model.Money;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductDescription;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.domain.model.ProductName;
import com.sleekydz86.catalogflow.domain.model.SupplierId;
import com.sleekydz86.catalogflow.global.exception.TransientBatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.jdbc.core.JdbcTemplate;

class ProductCsvItemWriterFailureTest {

	@Test
	@DisplayName("상품 저장 중 DB 오류는 일시 배치 예외로 변환한다")
	void shouldWrapDatabaseFailureAsTransientBatchException() {
		// given
		JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
		doAnswer(invocation -> {
			throw new RuntimeException("connection reset");
		}).when(jdbcTemplate).update(anyString(), any(Object[].class));
		ProductCsvItemWriter writer = new ProductCsvItemWriter(jdbcTemplate);
		Product product = Product.create(
				ProductId.generate(),
				new ProductName("장애상품"),
				new ProductDescription("설명"),
				new Money(BigDecimal.TEN, "KRW"),
				new CategoryId(UUID.randomUUID()),
				new SupplierId(UUID.randomUUID()),
				Instant.parse("2026-07-16T00:00:00Z"),
				"corr-batch");
		ProductImportItem item = new ProductImportItem("P-FAIL", product);

		// when / then
		TransientBatchException exception = assertThrows(
				TransientBatchException.class,
				() -> writer.write(Chunk.of(item)));
		assertTrue(exception.getMessage().contains("일시적 오류"));
	}
}
