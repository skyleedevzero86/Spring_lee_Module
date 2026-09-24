package com.sleekydz86.searchai.knowledge.application.service;

import com.sleekydz86.searchai.knowledge.application.port.in.SearchKnowledgeUseCase;
import com.sleekydz86.searchai.knowledge.application.port.in.UploadDocumentUseCase;
import com.sleekydz86.searchai.knowledge.application.port.out.DocumentParserPort;
import com.sleekydz86.searchai.knowledge.application.port.out.VectorStorePort;
import com.sleekydz86.searchai.knowledge.domain.KnowledgeChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
public final class DocumentKnowledgeService implements UploadDocumentUseCase, SearchKnowledgeUseCase {

	private static final Logger log = LoggerFactory.getLogger(DocumentKnowledgeService.class);

	private final DocumentParserPort documentParserPort;
	private final VectorStorePort vectorStorePort;

	public DocumentKnowledgeService(DocumentParserPort documentParserPort, VectorStorePort vectorStorePort) {
		this.documentParserPort = documentParserPort;
		this.vectorStorePort = vectorStorePort;
	}

	@Override
	public Mono<Void> upload(String fileName, Flux<DataBuffer> content) {
		log.info("문서 로딩 시작: {}", fileName);
		return DataBufferUtils.join(content)
			.map(buffer -> {
				byte[] bytes = new byte[buffer.readableByteCount()];
				buffer.read(bytes);
				DataBufferUtils.release(buffer);
				return bytes;
			})
			.flatMap(bytes -> documentParserPort.parse(new NamedByteArrayResource(bytes, fileName), fileName))
			.flatMap(chunks -> {
				log.info("문서 분할 완료: 분할 {}개", chunks.size());
				return vectorStorePort.store(chunks);
			})
			.doOnSuccess(ignored -> log.info("문서 로딩 완료: {}", fileName))
			.doOnError(error -> log.error("문서 로딩 실패: {}", fileName, error))
			.subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public Mono<List<KnowledgeChunk>> search(String query) {
		log.info("벡터 검색 실행: '{}'", query);
		return vectorStorePort.similaritySearch(query, 20)
			.doOnNext(results -> log.info("검색 결과 수: {}", results.size()))
			.subscribeOn(Schedulers.boundedElastic());
	}

	private static final class NamedByteArrayResource extends ByteArrayResource {

		private final String fileName;

		private NamedByteArrayResource(byte[] byteArray, String fileName) {
			super(byteArray);
			this.fileName = fileName;
		}

		@Override
		public String getFilename() {
			return fileName;
		}
	}
}
