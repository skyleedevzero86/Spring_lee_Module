package com.sleekydz86.searchai.gateway.chat.adapter.in.web;

import com.sleekydz86.searchai.gateway.chat.adapter.out.grpc.GrpcChatStreamAdapter;
import com.sleekydz86.searchai.gateway.global.security.AuthUser;
import com.sleekydz86.searchai.gateway.global.security.ChatModeAuthorization;
import com.sleekydz86.searchai.gateway.global.security.UserRole;
import com.sleekydz86.searchai.gateway.global.sse.SseHub;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping
public final class ChatController {

	private final GrpcChatStreamAdapter grpcChatStreamAdapter;
	private final SseHub sseHub;
	private final ChatModeAuthorization chatModeAuthorization;

	public ChatController(
		GrpcChatStreamAdapter grpcChatStreamAdapter,
		SseHub sseHub,
		ChatModeAuthorization chatModeAuthorization
	) {
		this.grpcChatStreamAdapter = grpcChatStreamAdapter;
		this.sseHub = sseHub;
		this.chatModeAuthorization = chatModeAuthorization;
	}

	@PostMapping("/chat/send")
	public Mono<Void> chat(@Valid @RequestBody ChatRequest request, Authentication authentication) {
		AuthUser user = (AuthUser) authentication.getPrincipal();
		chatModeAuthorization.assertAllowed(user.role(), request.mode());
		String routingMode = request.routingMode() == null ? "AUTO" : request.routingMode();
		if ("MANUAL".equalsIgnoreCase(routingMode) && user.role() != UserRole.ADMIN) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수동 모델 지정은 관리자만 가능합니다");
		}
		String streamUserId = user.username() + ":" + request.currentUserName();
		return grpcChatStreamAdapter.streamToSse(
			streamUserId,
			user.username(),
			request.message(),
			request.mode(),
			routingMode,
			request.forcedTier()
		);
	}

	@GetMapping(value = "/sse/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<String>> connect(
		Authentication authentication,
		@RequestParam(required = false) String sessionId
	) {
		AuthUser user = (AuthUser) authentication.getPrincipal();
		String streamUserId = user.username() + ":" + (sessionId == null || sessionId.isBlank() ? "default" : sessionId);
		return sseHub.connect(streamUserId)
			.map(payload -> ServerSentEvent.<String>builder()
				.event(payload.eventType())
				.data(payload.content())
				.build());
	}

	public record ChatRequest(
		@NotBlank String currentUserName,
		@NotBlank String message,
		String botMsgId,
		String mode,
		String routingMode,
		String forcedTier
	) {
	}
}
