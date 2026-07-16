package com.sleekydz86.catalogflow.adapter.in.batch.csv;

import java.time.Clock;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.batch.model.ProductCsvRow;
import com.sleekydz86.catalogflow.application.batch.model.ProductImportItem;
import com.sleekydz86.catalogflow.domain.model.CategoryId;
import com.sleekydz86.catalogflow.domain.model.Money;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductDescription;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.domain.model.ProductName;
import com.sleekydz86.catalogflow.domain.model.SupplierId;
import com.sleekydz86.catalogflow.global.exception.ProductCsvValidationException;
import com.sleekydz86.catalogflow.global.exception.TransientBatchException;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductCsvItemProcessor implements ItemProcessor<ProductCsvRow, ProductImportItem> {

	private final JdbcTemplate jdbcTemplate;
	private final Clock clock;

	public ProductCsvItemProcessor(JdbcTemplate jdbcTemplate, Clock clock) {
		this.jdbcTemplate = jdbcTemplate;
		this.clock = clock;
	}

	@Override
	public ProductImportItem process(ProductCsvRow item) {
		validate(item);
		assertProductCodeNotDuplicated(item.productCode());
		assertSupplierAvailable(item.supplierId());
		Product product = Product.create(
				new ProductId(UUID.randomUUID()),
				new ProductName(item.name().trim()),
				new ProductDescription(item.description() == null ? "" : item.description().trim()),
				new Money(item.priceAmount(), item.priceCurrency().trim().toUpperCase()),
				new CategoryId(item.categoryId()),
				new SupplierId(item.supplierId()),
				clock.instant(),
				CorrelationIdHolder.getOrGenerate());
		return new ProductImportItem(item.productCode().trim(), product);
	}

	private void validate(ProductCsvRow item) {
		if (item.productCode() == null || item.productCode().isBlank()) {
			throw new ProductCsvValidationException("상품 코드는 필수입니다");
		}
		if (item.name() == null || item.name().isBlank()) {
			throw new ProductCsvValidationException("상품명은 필수입니다");
		}
		if (item.priceAmount() == null || item.priceAmount().signum() < 0) {
			throw new ProductCsvValidationException("가격이 올바르지 않습니다");
		}
		if (item.priceCurrency() == null || item.priceCurrency().isBlank()) {
			throw new ProductCsvValidationException("통화 코드는 필수입니다");
		}
		if (item.categoryId() == null) {
			throw new ProductCsvValidationException("카테고리 ID는 필수입니다");
		}
		if (item.supplierId() == null) {
			throw new ProductCsvValidationException("공급사 ID는 필수입니다");
		}
	}

	private void assertProductCodeNotDuplicated(String productCode) {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM batch_import_product_codes WHERE product_code = ?",
				Integer.class,
				productCode.trim());
		if (count != null && count > 0) {
			throw new ProductCsvValidationException("이미 등록된 상품 코드입니다: " + productCode);
		}
	}

	private void assertSupplierAvailable(UUID supplierId) {
		try {
			Boolean available = jdbcTemplate.queryForObject(
					"SELECT available FROM suppliers WHERE id = ?",
					Boolean.class,
					supplierId);
			if (available == null) {
				throw new ProductCsvValidationException("공급사를 찾을 수 없습니다: " + supplierId);
			}
			if (!available) {
				throw new ProductCsvValidationException("사용 불가 공급사입니다: " + supplierId);
			}
		}
		catch (ProductCsvValidationException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new TransientBatchException("공급사 조회 중 일시적 오류가 발생했습니다", exception);
		}
	}
}
