package com.sleekydz86.catalogflow.adapter.in.messaging;

import com.rabbitmq.client.Channel;
import com.sleekydz86.catalogflow.application.service.ProductEventIngestionService;
import com.sleekydz86.catalogflow.eventcontract.CatalogQueues;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventParseException;
import com.sleekydz86.catalogflow.eventcontract.MessagingHeaders;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.messaging.consumer-enabled", havingValue = "true", matchIfMissing = true)
public class ProductEventListener {

	private final IntegrationEventMessageReader integrationEventMessageReader;
	private final ProductEventIngestionService productEventIngestionService;
	private final MessagingRetryHandler messagingRetryHandler;

	public ProductEventListener(
			IntegrationEventMessageReader integrationEventMessageReader,
			ProductEventIngestionService productEventIngestionService,
			MessagingRetryHandler messagingRetryHandler) {
		this.integrationEventMessageReader = integrationEventMessageReader;
		this.productEventIngestionService = productEventIngestionService;
		this.messagingRetryHandler = messagingRetryHandler;
	}

	@RabbitListener(queues = CatalogQueues.QUERY_PRODUCT_EVENTS, containerFactory = "rabbitListenerContainerFactory")
	public void onProductEvent(Message message, Channel channel) throws Exception {
		long deliveryTag = message.getMessageProperties().getDeliveryTag();
		int retryCount = integrationEventMessageReader.readRetryCount(message);
		try {
			integrationEventMessageReader.bindContext(message);
			var envelope = integrationEventMessageReader.read(message);
			productEventIngestionService.ingest(envelope);
			channel.basicAck(deliveryTag, false);
		}
		catch (IntegrationEventParseException exception) {
			messagingRetryHandler.handleFailure(message, retryCount, exception);
			channel.basicAck(deliveryTag, false);
		}
		catch (Exception exception) {
			messagingRetryHandler.handleFailure(message, retryCount, exception);
			channel.basicAck(deliveryTag, false);
		}
		finally {
			integrationEventMessageReader.clearContext();
		}
	}
}
