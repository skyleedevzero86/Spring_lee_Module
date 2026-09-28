package com.sleekydz86.productrecommend.application.service;

import com.sleekydz86.productrecommend.application.port.out.OutboxPort;
import com.sleekydz86.productrecommend.domain.outbox.OutboxMessage;
import com.sleekydz86.productrecommend.domain.outbox.OutboxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelayScheduler {

	private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);

	private final OutboxPort outboxPort;
	private final ProductEmbeddingListener embeddingListener;
	private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplate;
	private final boolean kafkaEnabled;
	private final String kafkaTopic;

	public OutboxRelayScheduler(
		OutboxPort outboxPort,
		ProductEmbeddingListener embeddingListener,
		ObjectProvider<KafkaTemplate<String, String>> kafkaTemplate,
		@org.springframework.beans.factory.annotation.Value("${app.outbox.kafka-enabled:false}") boolean kafkaEnabled,
		@org.springframework.beans.factory.annotation.Value("${app.outbox.kafka-topic:product-recommend.events}") String kafkaTopic
	) {
		this.outboxPort = outboxPort;
		this.embeddingListener = embeddingListener;
		this.kafkaTemplate = kafkaTemplate;
		this.kafkaEnabled = kafkaEnabled;
		this.kafkaTopic = kafkaTopic;
	}

	@Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:2000}")
	@Transactional
	public void relay() {
		List<OutboxMessage> pending = outboxPort.findByStatus(OutboxStatus.PENDING, 50);
		for (OutboxMessage message : pending) {
			try {
				if ("PRODUCT_EMBEDDING_REQUESTED".equals(message.eventType())) {
					Long productId = Long.valueOf(message.aggregateId());
					embeddingListener.embedProduct(productId);
				}
				if (kafkaEnabled) {
					KafkaTemplate<String, String> template = kafkaTemplate.getIfAvailable();
					if (template != null) {
						template.send(kafkaTopic, message.aggregateId(), message.payload());
					}
				}
				outboxPort.save(message.markPublished());
			} catch (Exception ex) {
				log.warn("Outbox 처리 실패 id={}: {}", message.id(), ex.getMessage());
				outboxPort.save(message.markFailed());
			}
		}
	}
}
