package com.sleekydz86.catalogflow.adapter.in.messaging;

import com.rabbitmq.client.Channel;
import com.sleekydz86.catalogflow.application.service.ProductEnrichmentProcessingService;
import com.sleekydz86.catalogflow.eventcontract.CatalogQueues;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventParseException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.consumer-enabled", havingValue = "true", matchIfMissing = true)
public class EnrichmentRequestListener {

	private final IntegrationEventMessageReader integrationEventMessageReader;
	private final ProductEnrichmentProcessingService productEnrichmentProcessingService;

	public EnrichmentRequestListener(
			IntegrationEventMessageReader integrationEventMessageReader,
			ProductEnrichmentProcessingService productEnrichmentProcessingService) {
		this.integrationEventMessageReader = integrationEventMessageReader;
		this.productEnrichmentProcessingService = productEnrichmentProcessingService;
	}

	@RabbitListener(queues = CatalogQueues.AI_ENRICHMENT_REQUESTS, containerFactory = "rabbitListenerContainerFactory")
	public void onEnrichmentRequested(Message message, Channel channel) throws Exception {
		long deliveryTag = message.getMessageProperties().getDeliveryTag();
		try {
			integrationEventMessageReader.bindContext(message);
			var envelope = integrationEventMessageReader.read(message);
			productEnrichmentProcessingService.process(envelope);
			channel.basicAck(deliveryTag, false);
		}
		catch (IntegrationEventParseException exception) {
			channel.basicNack(deliveryTag, false, false);
		}
		catch (Exception exception) {
			channel.basicNack(deliveryTag, false, false);
		}
		finally {
			integrationEventMessageReader.clearContext();
		}
	}
}
