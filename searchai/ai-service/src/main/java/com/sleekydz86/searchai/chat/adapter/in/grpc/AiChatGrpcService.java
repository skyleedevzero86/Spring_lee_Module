package com.sleekydz86.searchai.chat.adapter.in.grpc;

import com.sleekydz86.searchai.chat.application.port.in.StreamChatUseCase;
import com.sleekydz86.searchai.chat.domain.ChatMode;
import com.sleekydz86.searchai.chat.domain.ChatQuery;
import com.sleekydz86.searchai.chat.domain.StreamChunk;
import com.sleekydz86.searchai.global.grpc.GrpcAuthContextInterceptor;
import com.sleekydz86.searchai.proto.AiChatServiceGrpc;
import com.sleekydz86.searchai.proto.ChatChunk;
import com.sleekydz86.searchai.proto.ChatRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;
import reactor.core.Disposable;

import java.util.concurrent.atomic.AtomicLong;

@GrpcService
public final class AiChatGrpcService extends AiChatServiceGrpc.AiChatServiceImplBase {

	private static final Logger log = LoggerFactory.getLogger(AiChatGrpcService.class);

	private final StreamChatUseCase streamChatUseCase;

	public AiChatGrpcService(StreamChatUseCase streamChatUseCase) {
		this.streamChatUseCase = streamChatUseCase;
	}

	@Override
	public void streamChat(ChatRequest request, StreamObserver<ChatChunk> responseObserver) {
		if (request.getMessage() == null || request.getMessage().isBlank()) {
			log.warn("gRPC 채팅 거부: 메시지가 비어 있습니다");
			responseObserver.onError(Status.INVALID_ARGUMENT
				.withDescription("메시지가 비어 있습니다")
				.asRuntimeException());
			return;
		}

		String ctxUsername = GrpcAuthContextInterceptor.USERNAME.get();
		String ctxUserId = GrpcAuthContextInterceptor.USER_ID.get();
		String userId = firstNonBlank(request.getUserId(), ctxUserId, "anonymous");
		String username = firstNonBlank(request.getUsername(), ctxUsername, userId);

		ChatQuery query = ChatQuery.of(
			userId,
			username,
			request.getMessage(),
			toDomainMode(request.getMode()),
			request.getRoutingMode(),
			request.getForcedTier()
		);

		AtomicLong sequence = new AtomicLong();
		Disposable subscription = streamChatUseCase.streamChat(query)
			.subscribe(
				chunk -> {
					try {
						responseObserver.onNext(toProto(chunk, sequence));
					} catch (Exception ex) {
						log.error("gRPC 청크 전송 실패: userId={} 원인={}", userId, ex.getMessage(), ex);
						safeError(responseObserver, ex);
					}
				},
				error -> {
					log.error("gRPC 채팅 스트림 오류: userId={} 원인={}", userId, error.getMessage(), error);
					safeError(responseObserver, error);
				},
				() -> {
					try {
						responseObserver.onCompleted();
					} catch (Exception ex) {
						log.warn("gRPC 완료 통지 실패: userId={} 원인={}", userId, ex.getMessage());
					}
				}
			);

		io.grpc.Context.current().addListener(context -> {
			if (!subscription.isDisposed()) {
				subscription.dispose();
				log.info("gRPC 채팅 구독 취소: userId={}", userId);
			}
		}, Runnable::run);
	}

	private static void safeError(StreamObserver<ChatChunk> responseObserver, Throwable error) {
		try {
			responseObserver.onError(Status.INTERNAL
				.withDescription(error.getMessage() == null ? "내부 오류" : error.getMessage())
				.withCause(error)
				.asRuntimeException());
		} catch (Exception ex) {
			log.warn("gRPC 오류 응답 전송 실패: 원인={}", ex.getMessage());
		}
	}

	private static String firstNonBlank(String... values) {
		if (values == null) {
			return "";
		}
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return "";
	}

	private static ChatMode toDomainMode(com.sleekydz86.searchai.proto.ChatMode mode) {
		return switch (mode) {
			case KNOWLEDGE_BASE -> ChatMode.KNOWLEDGE_BASE;
			case INTERNET_SEARCH -> ChatMode.INTERNET_SEARCH;
			case DIRECT, CHAT_MODE_UNSPECIFIED, UNRECOGNIZED -> ChatMode.DIRECT;
		};
	}

	private static ChatChunk toProto(StreamChunk chunk, AtomicLong sequence) {
		long seq = chunk.sequence() > 0 ? chunk.sequence() : sequence.incrementAndGet();
		ChatChunk.Builder builder = ChatChunk.newBuilder()
			.setContent(chunk.content() == null ? "" : chunk.content())
			.setEventType(chunk.type().name())
			.setRequestId(chunk.requestId() == null ? "" : chunk.requestId())
			.setSequence(seq);
		if (chunk.finishReason() != null && !chunk.finishReason().isBlank()) {
			builder.setFinishReason(chunk.finishReason());
		} else if (chunk.type().name().equals("FINISH")) {
			builder.setFinishReason(chunk.content() == null || chunk.content().isBlank() ? "done" : chunk.content());
		}
		return builder.build();
	}
}
