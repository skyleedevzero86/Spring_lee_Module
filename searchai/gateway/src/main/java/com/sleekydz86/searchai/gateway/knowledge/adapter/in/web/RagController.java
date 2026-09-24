package com.sleekydz86.searchai.gateway.knowledge.adapter.in.web;

import com.sleekydz86.searchai.gateway.global.security.AuthUser;
import com.sleekydz86.searchai.gateway.global.security.ChatModeAuthorization;
import com.sleekydz86.searchai.gateway.global.web.LeeResult;
import com.sleekydz86.searchai.gateway.knowledge.adapter.out.grpc.GrpcKnowledgeAdapter;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rag")
public final class RagController {

	private final GrpcKnowledgeAdapter grpcKnowledgeAdapter;
	private final ChatModeAuthorization chatModeAuthorization;

	public RagController(GrpcKnowledgeAdapter grpcKnowledgeAdapter, ChatModeAuthorization chatModeAuthorization) {
		this.grpcKnowledgeAdapter = grpcKnowledgeAdapter;
		this.chatModeAuthorization = chatModeAuthorization;
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Mono<LeeResult<Void>> upload(@RequestPart("file") FilePart file, Authentication authentication) {
		AuthUser user = (AuthUser) authentication.getPrincipal();
		String fileName = file.filename() == null || file.filename().isBlank() ? "unknown.pdf" : file.filename();
		chatModeAuthorization.assertPdfUpload(user.role(), fileName);

		return DataBufferUtils.join(file.content())
			.map(buffer -> {
				byte[] bytes = new byte[buffer.readableByteCount()];
				buffer.read(bytes);
				DataBufferUtils.release(buffer);
				return bytes;
			})
			.flatMap(bytes -> grpcKnowledgeAdapter.upload(fileName, bytes))
			.map(response -> response.getSuccess()
				? LeeResult.<Void>ok(response.getMessage())
				: LeeResult.<Void>error(response.getMessage()))
			.onErrorResume(error -> Mono.just(LeeResult.error("업로드 실패: " + error.getMessage())));
	}
}
