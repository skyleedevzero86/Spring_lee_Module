package com.sleekydz86.searchai.knowledge.adapter.in.grpc;

import com.google.protobuf.ByteString;
import com.sleekydz86.searchai.knowledge.application.port.in.UploadDocumentUseCase;
import com.sleekydz86.searchai.proto.KnowledgeServiceGrpc;
import com.sleekydz86.searchai.proto.UploadRequest;
import com.sleekydz86.searchai.proto.UploadResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.grpc.server.service.GrpcService;
import reactor.core.publisher.Flux;

@GrpcService
public final class KnowledgeGrpcService extends KnowledgeServiceGrpc.KnowledgeServiceImplBase {

	private static final Logger log = LoggerFactory.getLogger(KnowledgeGrpcService.class);

	private final UploadDocumentUseCase uploadDocumentUseCase;
	private final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

	public KnowledgeGrpcService(UploadDocumentUseCase uploadDocumentUseCase) {
		this.uploadDocumentUseCase = uploadDocumentUseCase;
	}

	@Override
	public void uploadDocument(UploadRequest request, StreamObserver<UploadResponse> responseObserver) {
		String fileName = request.getFileName().isBlank() ? "unknown" : request.getFileName();
		ByteString content = request.getContent();
		Flux<org.springframework.core.io.buffer.DataBuffer> buffers = Flux.just(
			bufferFactory.wrap(content.toByteArray())
		);

		uploadDocumentUseCase.upload(fileName, buffers)
			.subscribe(
				ignored -> {
				},
				error -> {
					log.error("문서 업로드 gRPC 실패: {}", fileName, error);
					responseObserver.onNext(UploadResponse.newBuilder()
						.setSuccess(false)
						.setMessage("업로드 실패: " + error.getMessage())
						.build());
					responseObserver.onCompleted();
				},
				() -> {
					responseObserver.onNext(UploadResponse.newBuilder()
						.setSuccess(true)
						.setMessage("파일이 성공적으로 업로드되었습니다.")
						.build());
					responseObserver.onCompleted();
				}
			);
	}
}
