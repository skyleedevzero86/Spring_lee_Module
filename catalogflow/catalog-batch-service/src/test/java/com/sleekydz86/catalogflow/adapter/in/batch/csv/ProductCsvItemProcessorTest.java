package com.sleekydz86.catalogflow.adapter.in.batch.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.batch.model.ProductCsvRow;
import com.sleekydz86.catalogflow.application.batch.model.ProductImportItem;
import com.sleekydz86.catalogflow.global.exception.ProductCsvValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class ProductCsvItemProcessorTest {

	private static final UUID CATEGORY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID SUPPLIER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Mock
	private JdbcTemplate jdbcTemplate;

	private ProductCsvItemProcessor processor;

	@BeforeEach
	void setUp() {
		processor = new ProductCsvItemProcessor(
				jdbcTemplate,
				Clock.fixed(Instant.parse("2026-07-16T00:00:00Z"), ZoneOffset.UTC));
	}

	@Test
	@DisplayName("유효한 CSV 행을 상품 도메인 객체로 변환한다")
	void shouldProcessValidCsvRow() {
		// given
		ProductCsvRow row = new ProductCsvRow(
				"P-100",
				"무선 마우스",
				"설명",
				BigDecimal.valueOf(19900),
				"KRW",
				CATEGORY_ID,
				SUPPLIER_ID,
				2);
		when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("P-100"))).thenReturn(0);
		when(jdbcTemplate.queryForObject(anyString(), eq(Boolean.class), eq(SUPPLIER_ID))).thenReturn(true);

		// when
		ProductImportItem item = processor.process(row);

		// then
		assertNotNull(item);
		assertEquals("P-100", item.productCode());
		assertEquals("무선 마우스", item.product().getName().value());
	}

	@Test
	@DisplayName("상품명이 비어 있으면 검증 예외를 발생시킨다")
	void shouldRejectBlankName() {
		// given
		ProductCsvRow row = new ProductCsvRow(
				"P-101",
				" ",
				"설명",
				BigDecimal.TEN,
				"KRW",
				CATEGORY_ID,
				SUPPLIER_ID,
				3);

		// when / then
		assertThrows(ProductCsvValidationException.class, () -> processor.process(row));
	}

	@Test
	@DisplayName("중복 상품 코드는 검증 예외를 발생시킨다")
	void shouldRejectDuplicateProductCode() {
		// given
		ProductCsvRow row = new ProductCsvRow(
				"P-102",
				"중복상품",
				"설명",
				BigDecimal.TEN,
				"KRW",
				CATEGORY_ID,
				SUPPLIER_ID,
				4);
		when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("P-102"))).thenReturn(1);

		// when / then
		ProductCsvValidationException exception = assertThrows(
				ProductCsvValidationException.class,
				() -> processor.process(row));
		assertEquals(true, exception.getMessage().contains("이미 등록된 상품 코드"));
	}

	@Test
	@DisplayName("음수 가격은 검증 예외를 발생시킨다")
	void shouldRejectNegativePrice() {
		// given
		ProductCsvRow row = new ProductCsvRow(
				"P-103",
				"음수가격",
				"설명",
				BigDecimal.valueOf(-1),
				"KRW",
				CATEGORY_ID,
				SUPPLIER_ID,
				5);

		// when / then
		assertThrows(ProductCsvValidationException.class, () -> processor.process(row));
	}
}
