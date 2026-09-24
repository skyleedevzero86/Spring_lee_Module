package com.sleekydz86.searchai.knowledge.application.port.in;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UploadDocumentUseCase {

	Mono<Void> upload(String fileName, Flux<DataBuffer> content);
}
