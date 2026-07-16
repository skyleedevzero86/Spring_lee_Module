package com.sleekydz86.catalogflow.global.config;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.amqp.rabbit.connection.CorrelationData;

public final class RabbitMqPublisherConfiguration {

	private RabbitMqPublisherConfiguration() {
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
