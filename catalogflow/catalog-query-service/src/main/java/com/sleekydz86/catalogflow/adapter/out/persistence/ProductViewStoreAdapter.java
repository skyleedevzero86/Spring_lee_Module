package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.out.ProductViewStore;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.stereotype.Component;

@Component
public class ProductViewStoreAdapter implements ProductViewStore {

	private final ProductViewMongoRepository productViewMongoRepository;

	public ProductViewStoreAdapter(ProductViewMongoRepository productViewMongoRepository) {
		this.productViewMongoRepository = productViewMongoRepository;
	}

	@Override
	public Optional<ProductView> findByProductId(UUID productId) {
		return productViewMongoRepository.findById(productId.toString()).map(ProductViewMapper::toDomain);
	}

	@Override
	public void save(ProductView productView) {
		try {
			productViewMongoRepository.save(ProductViewMapper.toDocument(productView));
		}
		catch (Exception exception) {
			throw new ApplicationException("상품 조회 모델 저장에 실패했습니다", exception);
		}
	}
}
