package com.sleekydz86.catalogflow.application.port.in;

import com.sleekydz86.catalogflow.application.query.ProductPageResult;
import com.sleekydz86.catalogflow.application.query.ProductQueryCriteria;

public interface ListProductsQueryUseCase {

	ProductPageResult list(ProductQueryCriteria criteria);
}
