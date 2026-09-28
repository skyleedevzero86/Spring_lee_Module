package com.sleekydz86.productrecommend.adapter.in.mcp;

import com.sleekydz86.productrecommend.application.port.in.ProductUseCase;
import com.sleekydz86.productrecommend.domain.product.Product;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ProductSearchTool {

	private final ProductUseCase productUseCase;

	public ProductSearchTool(ProductUseCase productUseCase) {
		this.productUseCase = productUseCase;
	}

	@Tool(description = "주어진 키워드와 유사한 상품을 KNN 벡터 검색으로 찾습니다. 검색 키워드와 의미적으로 유사한 상품의 이름과 키워드 목록을 반환합니다.")
	public List<ProductInfo> searchProducts(
		@ToolParam(description = "유사한 상품을 검색할 키워드들 (쉼표로 구분). 예: '노트북, 게이밍, 고성능'") String keywords,
		@ToolParam(description = "반환할 최대 상품 수. 기본값은 5입니다.") Integer k
	) {
		int limit = (k == null || k <= 0) ? 5 : k;
		List<String> keywordList = Arrays.stream(keywords.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.toList();
		return productUseCase.knnSearch(keywordList, limit).stream().map(ProductInfo::from).toList();
	}

	@Tool(description = "특정 상품 ID를 기준으로 유사한 상품을 찾습니다. 키워드 임베딩을 기반으로 비슷한 특성을 가진 상품들을 반환합니다.")
	public List<ProductInfo> findSimilarProducts(
		@ToolParam(description = "유사한 상품을 찾을 기준 상품의 ID") Long productId,
		@ToolParam(description = "반환할 최대 유사 상품 수. 기본값은 5입니다.") Integer k
	) {
		int limit = (k == null || k <= 0) ? 5 : k;
		return productUseCase.findSimilarProducts(productId, limit).stream().map(ProductInfo::from).toList();
	}

	@Tool(description = "상품 ID로 특정 상품의 상세 정보를 조회합니다.")
	public ProductInfo getProductById(
		@ToolParam(description = "조회할 상품의 ID") Long productId
	) {
		return productUseCase.findById(productId).map(ProductInfo::from).orElse(null);
	}

	@Tool(description = "시스템에 등록된 모든 상품 목록을 조회합니다.")
	public List<ProductInfo> getAllProducts() {
		return productUseCase.findAll().stream().map(ProductInfo::from).toList();
	}

	public record ProductInfo(Long id, String name, List<String> keywords, String embeddingStatus) {
		static ProductInfo from(Product product) {
			return new ProductInfo(
				product.id(),
				product.name(),
				product.keywords(),
				product.embeddingStatus() == null ? null : product.embeddingStatus().name()
			);
		}
	}
}
