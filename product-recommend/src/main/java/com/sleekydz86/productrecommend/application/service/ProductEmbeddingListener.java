package com.sleekydz86.productrecommend.application.service;

import com.sleekydz86.productrecommend.application.port.out.EmbeddingPort;
import com.sleekydz86.productrecommend.application.port.out.ProductRepositoryPort;
import com.sleekydz86.productrecommend.domain.product.EmbeddingStatus;
import com.sleekydz86.productrecommend.domain.product.Product;
import com.sleekydz86.productrecommend.domain.product.ProductEmbeddingRequested;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProductEmbeddingListener {

	private static final Logger log = LoggerFactory.getLogger(ProductEmbeddingListener.class);

	private final ProductRepositoryPort productRepositoryPort;
	private final EmbeddingPort embeddingPort;

	public ProductEmbeddingListener(
		ProductRepositoryPort productRepositoryPort,
		EmbeddingPort embeddingPort
	) {
		this.productRepositoryPort = productRepositoryPort;
		this.embeddingPort = embeddingPort;
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onEmbeddingRequested(ProductEmbeddingRequested event) {
		embedProduct(event.productId());
	}

	@Transactional
	public void embedProduct(Long productId) {
		productRepositoryPort.findById(productId).ifPresent(this::embedAndSave);
	}

	private void embedAndSave(Product product) {
		if (product.keywords().isEmpty()) {
			productRepositoryPort.save(product.withEmbedding(null, null, EmbeddingStatus.READY));
			return;
		}
		try {
			float[] embedding = embeddingPort.average(embeddingPort.embedAll(product.keywords()));
			productRepositoryPort.save(
				product.withEmbedding(embedding, embeddingPort.modelName(), EmbeddingStatus.READY)
			);
		} catch (Exception ex) {
			log.warn("임베딩 생성 실패 productId={}: {}", product.id(), ex.getMessage());
			productRepositoryPort.save(
				product.withEmbedding(null, embeddingPort.modelName(), EmbeddingStatus.FAILED)
			);
		}
	}
}
