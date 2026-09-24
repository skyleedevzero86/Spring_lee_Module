package com.sleekydz86.searchai.knowledge.application.port.in;

import com.sleekydz86.searchai.knowledge.domain.KnowledgeChunk;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SearchKnowledgeUseCase {

	Mono<List<KnowledgeChunk>> search(String query);
}
