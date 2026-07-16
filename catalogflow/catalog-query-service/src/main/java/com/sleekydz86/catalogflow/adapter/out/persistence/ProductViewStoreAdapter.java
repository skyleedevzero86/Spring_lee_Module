package com.sleekydz86.catalogflow.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.out.ProductViewStore;
import com.sleekydz86.catalogflow.application.query.ProductQueryCriteria;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class ProductViewStoreAdapter implements ProductViewStore {

	private final ProductViewMongoRepository productViewMongoRepository;
	private final MongoTemplate mongoTemplate;

	public ProductViewStoreAdapter(
			ProductViewMongoRepository productViewMongoRepository,
			MongoTemplate mongoTemplate) {
		this.productViewMongoRepository = productViewMongoRepository;
		this.mongoTemplate = mongoTemplate;
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

	@Override
	public List<ProductView> findByCriteria(ProductQueryCriteria criteria, int fetchSize) {
		try {
			Query query = new Query();
			List<Criteria> filters = new ArrayList<>();
			if (criteria.status() != null && !criteria.status().isBlank()) {
				filters.add(Criteria.where("status").is(criteria.status()));
			}
			if (criteria.categoryId() != null) {
				filters.add(Criteria.where("categoryId").is(criteria.categoryId().toString()));
			}
			if (criteria.name() != null && !criteria.name().isBlank()) {
				filters.add(Criteria.where("name").regex(Pattern.quote(criteria.name()), "i"));
			}
			if (criteria.keyword() != null && !criteria.keyword().isBlank()) {
				String keyword = Pattern.quote(criteria.keyword());
				filters.add(new Criteria().orOperator(
						Criteria.where("keywords").regex(keyword, "i"),
						Criteria.where("name").regex(keyword, "i"),
						Criteria.where("summary").regex(keyword, "i")));
			}
			if (criteria.minPrice() != null) {
				filters.add(Criteria.where("price").gte(criteria.minPrice()));
			}
			if (criteria.maxPrice() != null) {
				filters.add(Criteria.where("price").lte(criteria.maxPrice()));
			}
			if (criteria.cursorPublishedAt() != null || (criteria.cursorProductId() != null
					&& !criteria.cursorProductId().isBlank())) {
				filters.add(buildCursorCriteria(criteria.cursorPublishedAt(), criteria.cursorProductId()));
			}
			if (!filters.isEmpty()) {
				query.addCriteria(new Criteria().andOperator(filters.toArray(Criteria[]::new)));
			}
			query.with(Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("productId")));
			query.limit(fetchSize);
			return mongoTemplate.find(query, ProductViewDocument.class).stream()
					.map(ProductViewMapper::toDomain)
					.toList();
		}
		catch (ApplicationException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new ApplicationException("상품 조회 모델 검색에 실패했습니다", exception);
		}
	}

	private Criteria buildCursorCriteria(Instant cursorPublishedAt, String cursorProductId) {
		if (cursorPublishedAt == null) {
			return Criteria.where("productId").lt(cursorProductId);
		}
		if (cursorProductId == null || cursorProductId.isBlank()) {
			return Criteria.where("publishedAt").lt(cursorPublishedAt);
		}
		return new Criteria().orOperator(
				Criteria.where("publishedAt").lt(cursorPublishedAt),
				new Criteria().andOperator(
						Criteria.where("publishedAt").is(cursorPublishedAt),
						Criteria.where("productId").lt(cursorProductId)));
	}
}
