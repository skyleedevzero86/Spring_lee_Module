package com.sleekydz86.catalogflow.global.config;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqPublisherConfiguration {

	@Bean
	RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setMandatory(true);
		rabbitTemplate.setReturnsCallback(returned -> {
			throw new ApplicationException(
					"발행되지 않은 메시지가 반환되었습니다: " + returned.getReplyText());
		});
		rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
			if (!ack && correlationData != null) {
				correlationData.getFuture().completeExceptionally(
						new ApplicationException("메시지 발행 확인에 실패했습니다: " + cause));
			}
		});
		return rabbitTemplate;
	}

	public static void waitForPublisherConfirm(CorrelationData correlationData, long timeoutMs) {
		try {
			var confirm = correlationData.getFuture().get(timeoutMs, TimeUnit.MILLISECONDS);
			if (!confirm.ack()) {
				throw new ApplicationException("메시지 발행 확인이 거부되었습니다");
			}
		}
		catch (TimeoutException exception) {
			throw new ApplicationException("메시지 발행 확인 대기 시간이 초과되었습니다", exception);
		}
		catch (ApplicationException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new ApplicationException("메시지 발행 확인 처리에 실패했습니다", exception);
		}
	}
}
