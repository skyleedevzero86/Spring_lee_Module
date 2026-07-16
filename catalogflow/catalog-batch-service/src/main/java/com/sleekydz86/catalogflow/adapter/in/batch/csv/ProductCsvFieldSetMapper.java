package com.sleekydz86.catalogflow.adapter.in.batch.csv;

import java.math.BigDecimal;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.batch.model.ProductCsvRow;
import com.sleekydz86.catalogflow.global.exception.ProductCsvValidationException;
import org.springframework.batch.infrastructure.item.file.mapping.FieldSetMapper;
import org.springframework.batch.infrastructure.item.file.transform.FieldSet;

public class ProductCsvFieldSetMapper implements FieldSetMapper<ProductCsvRow> {

	@Override
	public ProductCsvRow mapFieldSet(FieldSet fieldSet) {
		try {
			String productCode = fieldSet.readString("productCode");
			String name = fieldSet.readString("name");
			String description = fieldSet.readString("description");
			BigDecimal priceAmount = fieldSet.readBigDecimal("priceAmount");
			String priceCurrency = fieldSet.readString("priceCurrency");
			UUID categoryId = UUID.fromString(fieldSet.readString("categoryId"));
			UUID supplierId = UUID.fromString(fieldSet.readString("supplierId"));
			return new ProductCsvRow(
					productCode,
					name,
					description,
					priceAmount,
					priceCurrency,
					categoryId,
					supplierId,
					fieldSet.getFieldCount());
		}
		catch (ProductCsvValidationException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new ProductCsvValidationException("CSV 행 매핑에 실패했습니다: " + exception.getMessage());
		}
	}
}
