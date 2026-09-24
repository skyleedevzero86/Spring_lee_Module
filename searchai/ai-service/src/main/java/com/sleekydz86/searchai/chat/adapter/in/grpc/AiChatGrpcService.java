package com.sleekydz86.searchai.chat.adapter.in.grpc;

import com.sleekydz86.searchai.chat.application.port.in.StreamChatUseCase;
import com.sleekydz86.searchai.chat.domain.ChatMode;
import com.sleekydz86.searchai.chat.domain.ChatQuery;
import com.sleekydz86.searchai.chat.domain.StreamChunk;
import com.sleekydz86.searchai.proto.AiChatServiceGrpc;
import com.sleekydz86.searchai.proto.ChatChunk;
import com.sleekydz86.searchai.proto.ChatRequest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;
import reactor.core.Disposable;

@GrpcService
public final class AiChatGrpcService extends AiChatServiceGrpc.AiChatServiceImplBase {

	private static final Logger log = LoggerFactory.getLogger(AiChatGrpcService.class);

	private final StreamChatUseCase streamChatUseCase;

	public AiChatGrpcService(StreamChatUseCase streamChatUseCase) {
		this.streamChatUseCase = streamChatUseCase;
	}

	@Override
	public void streamChat(ChatRequest request, StreamObserver<ChatChunk> responseObserver) {
		ChatQuery query = ChatQuery.of(
			request.getUserId(),
			request.getUsername().isBlank() ? request.getUserId() : request.getUsername(),
			request.getMessage(),
			toDomainMode(request.getMode()),
			request.getRoutingMode(),
			request.getForcedTier()
		);

		Disposable subscription = streamChatUseCase.streamChat(query)
			.subscribe(
				chunk -> responseObserver.onNext(toProto(chunk)),
				error -> {
					log.error("gRPC 채팅 스트림 오류: {}", error.getMessage(), error);
					responseObserver.onError(error);
				},
				responseObserver::onCompleted
			);

		io.grpc.Context.current().addListener(context -> {
			if (!subscription.isDisposed()) {
				subscription.dispose();
			}
		}, Runnable::run);
	}

	private static ChatMode toDomainMode(com.sleekydz86.searchai.proto.ChatMode mode) {
		return switch (mode) {
			case KNOWLEDGE_BASE -> ChatMode.KNOWLEDGE_BASE;
			case INTERNET_SEARCH -> ChatMode.INTERNET_SEARCH;
			case DIRECT, CHAT_MODE_UNSPECIFIED, UNRECOGNIZED -> ChatMode.DIRECT;
		};
	}

	private static ChatChunk toProto(StreamChunk chunk) {
		return ChatChunk.newBuilder()
			.setContent(chunk.content())
			.setEventType(chunk.type().name())
			.build();
	}
}
