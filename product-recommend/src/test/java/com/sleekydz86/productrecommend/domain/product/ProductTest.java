package com.sleekydz86.productrecommend.domain.product;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

	@Test
	void createsProductWithPendingEmbedding() {
		Product product = Product.create(
			"무선 이어폰",
			List.of("오디오", "무선"),
			"AUDIO",
			"APPLE",
			"WHITE",
			BigDecimal.valueOf(200000),
			5
		);
		assertThat(product.name()).isEqualTo("무선 이어폰");
		assertThat(product.keywords()).containsExactly("오디오", "무선");
		assertThat(product.category()).isEqualTo("AUDIO");
		assertThat(product.id()).isNull();
		assertThat(product.embeddingStatus()).isEqualTo(EmbeddingStatus.PENDING);
		assertThat(product.embedding()).isNull();
		assertThat(product.available()).isTrue();
	}

	@Test
	void rejectsBlankName() {
		assertThatThrownBy(() -> Product.create(" ", List.of(), null, null, null, null, 0))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("상품명");
	}
}
