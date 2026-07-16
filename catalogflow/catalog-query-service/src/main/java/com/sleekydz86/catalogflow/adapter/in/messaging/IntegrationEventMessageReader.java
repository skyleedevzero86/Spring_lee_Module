package com.sleekydz86.catalogflow.adapter.in.messaging;

import java.nio.charset.StandardCharsets;

import com.sleekydz86.catalogflow.eventcontract.IntegrationEventEnvelope;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventJsonMapper;
import com.sleekydz86.catalogflow.eventcontract.IntegrationEventParseException;
import com.sleekydz86.catalogflow.eventcontract.MessagingHeaders;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import com.sleekydz86.catalogflow.global.util.TraceIdHolder;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Component
public class IntegrationEventMessageReader {

	public IntegrationEventEnvelope read(Message message) {
		try {
			String json = new String(message.getBody(), StandardCharsets.UTF_8);
			return IntegrationEventJsonMapper.read(json);
		}
		catch (IntegrationEventParseException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new IntegrationEventParseException("메시지 본문을 읽을 수 없습니다", exception);
		}
	}

	public void bindContext(Message message) {
		Object correlationId = message.getMessageProperties().getHeader(MessagingHeaders.CORRELATION_ID);
		if (correlationId != null) {
			CorrelationIdHolder.set(correlationId.toString());
		}
		Object traceId = message.getMessageProperties().getHeader(MessagingHeaders.TRACE_ID);
		if (traceId != null) {
			TraceIdHolder.set(traceId.toString());
		}
	}

	public void clearContext() {
		CorrelationIdHolder.clear();
		TraceIdHolder.clear();
	}

	public int readRetryCount(Message message) {
		Object retryCount = message.getMessageProperties().getHeader(MessagingHeaders.RETRY_COUNT);
		if (retryCount == null) {
			return 0;
		}
		if (retryCount instanceof Number number) {
			return number.intValue();
		}
		try {
			return Integer.parseInt(retryCount.toString());
		}
		catch (NumberFormatException exception) {
			throw new ApplicationException("재시도 횟수 헤더가 유효하지 않습니다");
		}
	}
}
