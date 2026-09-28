package com.sleekydz86.productrecommend.adapter.in.mcp;

import com.sleekydz86.productrecommend.application.port.in.ProductUseCase;
import com.sleekydz86.productrecommend.domain.product.Product;
import com.sleekydz86.productrecommend.domain.recommend.RankedRecommendation;
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

	@Tool(description = "자연어 질의를 조건 검색+개인화 추천 파이프라인으로 실행합니다. 점수와 추천 근거를 함께 반환합니다.")
	public List<RecommendedProduct> recommendProducts(
		@ToolParam(description = "추천 요청 문장. 예: '10만원 이하 검정 러닝화, 나이키 말고'") String query,
		@ToolParam(description = "사용자 ID. 없으면 anonymous") String userId,
		@ToolParam(description = "반환 개수") Integer k
	) {
		int limit = (k == null || k <= 0) ? 5 : k;
		String safeUser = (userId == null || userId.isBlank()) ? "anonymous" : userId;
		return productUseCase.recommend(safeUser, query, limit).stream()
			.map(RecommendedProduct::from)
			.toList();
	}

	@Tool(description = "주어진 키워드와 유사한 상품을 KNN 벡터 검색으로 찾습니다.")
	public List<ProductInfo> searchProducts(
		@ToolParam(description = "검색 키워드들 (쉼표로 구분)") String keywords,
		@ToolParam(description = "반환할 최대 상품 수") Integer k
	) {
		int limit = (k == null || k <= 0) ? 5 : k;
		List<String> keywordList = Arrays.stream(keywords.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.toList();
		return productUseCase.knnSearch(keywordList, limit).stream().map(ProductInfo::from).toList();
	}

	@Tool(description = "특정 상품 ID를 기준으로 유사한 상품을 찾습니다.")
	public List<ProductInfo> findSimilarProducts(
		@ToolParam(description = "기준 상품 ID") Long productId,
		@ToolParam(description = "반환할 최대 유사 상품 수") Integer k
	) {
		int limit = (k == null || k <= 0) ? 5 : k;
		return productUseCase.findSimilarProducts(productId, limit).stream().map(ProductInfo::from).toList();
	}

	@Tool(description = "상품 ID로 특정 상품의 상세 정보를 조회합니다.")
	public ProductInfo getProductById(@ToolParam(description = "조회할 상품 ID") Long productId) {
		return productUseCase.findById(productId).map(ProductInfo::from).orElse(null);
	}

	@Tool(description = "시스템에 등록된 모든 상품 목록을 조회합니다.")
	public List<ProductInfo> getAllProducts() {
		return productUseCase.findAll().stream().map(ProductInfo::from).toList();
	}

	public record ProductInfo(
		Long id,
		String name,
		List<String> keywords,
		String category,
		String brand,
		String embeddingStatus
	) {
		static ProductInfo from(Product product) {
			return new ProductInfo(
				product.id(),
				product.name(),
				product.keywords(),
				product.category(),
				product.brand(),
				product.embeddingStatus() == null ? null : product.embeddingStatus().name()
			);
		}
	}

	public record RecommendedProduct(
		Long id,
		String name,
		double score,
		List<String> reasons
	) {
		static RecommendedProduct from(RankedRecommendation ranked) {
			return new RecommendedProduct(
				ranked.product().id(),
				ranked.product().name(),
				ranked.score(),
				ranked.reasons()
			);
		}
	}
}
