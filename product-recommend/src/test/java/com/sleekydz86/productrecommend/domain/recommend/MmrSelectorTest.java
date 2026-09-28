package com.sleekydz86.productrecommend.domain.recommend;

import com.sleekydz86.productrecommend.domain.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MmrSelectorTest {

	@Test
	void diversifiesBrands() {
		List<RankedRecommendation> input = List.of(
			item("Nike A", "NIKE", 0.95),
			item("Nike B", "NIKE", 0.94),
			item("Adidas A", "ADIDAS", 0.90),
			item("Asics A", "ASICS", 0.88)
		);
		List<RankedRecommendation> selected = MmrSelector.select(input, 3, 0.5);
		assertThat(selected).hasSize(3);
		assertThat(selected.stream().map(r -> r.product().brand()).distinct().count()).isGreaterThanOrEqualTo(2);
	}

	private static RankedRecommendation item(String name, String brand, double score) {
		Product product = Product.create(name, List.of("러닝"), "RUNNING_SHOES", brand, "BLACK", BigDecimal.TEN, 5);
		product = Product.rehydrate(
			Math.abs(name.hashCode() % 1000) + 1L,
			product.name(),
			product.keywords(),
			product.category(),
			product.brand(),
			product.color(),
			product.price(),
			product.stock(),
			product.status(),
			0,
			product.createdAt(),
			null,
			product.embeddingStatus(),
			null
		);
		return new RankedRecommendation(product, score, List.of("test"));
	}
}
