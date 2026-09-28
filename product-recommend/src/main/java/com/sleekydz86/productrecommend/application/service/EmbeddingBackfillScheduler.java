package com.sleekydz86.productrecommend.application.service;

import com.sleekydz86.productrecommend.application.port.in.ProductUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.embedding", name = "backfill-enabled", havingValue = "true")
public class EmbeddingBackfillScheduler {

	private static final Logger log = LoggerFactory.getLogger(EmbeddingBackfillScheduler.class);

	private final ProductUseCase productUseCase;

	public EmbeddingBackfillScheduler(ProductUseCase productUseCase) {
		this.productUseCase = productUseCase;
	}

	@Scheduled(fixedDelayString = "${app.embedding.backfill-interval-ms:300000}")
	public void backfillMissingEmbeddings() {
		int queued = productUseCase.retryPendingEmbeddings();
		if (queued > 0) {
			log.info("임베딩 백필 큐잉: {}건 (PENDING/FAILED/embedding IS NULL)", queued);
		}
	}
}
