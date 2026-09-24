package com.sleekydz86.searchai.gateway.global.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class SseHub {

	private static final Logger log = LoggerFactory.getLogger(SseHub.class);

	private final Map<String, Sinks.Many<TypedPayload>> sinks = new ConcurrentHashMap<>();

	public Flux<TypedPayload> connect(String userId) {
		Sinks.Many<TypedPayload> sink = sinks.compute(userId, (key, existing) -> {
			if (existing != null) {
				existing.tryEmitComplete();
			}
			return Sinks.many().multicast().onBackpressureBuffer();
		});
		log.info("SSE 연결 생성: {}", userId);
		return sink.asFlux()
			.doFinally(signal -> {
				sinks.remove(userId, sink);
				log.info("SSE 연결 종료: {} ({})", userId, signal);
			});
	}

	public Mono<Void> send(String userId, String eventType, String content) {
		return Mono.fromRunnable(() -> {
			Sinks.Many<TypedPayload> sink = sinks.get(userId);
			if (sink == null) {
				log.warn("SSE 대상 연결이 없습니다: {}", userId);
				return;
			}
			Sinks.EmitResult result = sink.tryEmitNext(new TypedPayload(eventType, content));
			if (result.isFailure()) {
				log.warn("SSE 전송 실패: {} / {}", userId, result);
			}
		});
	}

	public Mono<Void> close(String userId) {
		return Mono.fromRunnable(() -> {
			Sinks.Many<TypedPayload> sink = sinks.remove(userId);
			if (sink != null) {
				sink.tryEmitComplete();
			}
		});
	}

	public record TypedPayload(String eventType, String content) {
	}
}
