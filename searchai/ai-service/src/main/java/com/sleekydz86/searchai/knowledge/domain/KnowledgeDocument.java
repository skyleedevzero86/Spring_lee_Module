package com.sleekydz86.searchai.knowledge.domain;

import java.util.List;

public record KnowledgeDocument(String fileName, List<KnowledgeChunk> chunks) {

	public KnowledgeDocument {
		if (fileName == null || fileName.isBlank()) {
			throw new IllegalArgumentException("파일명은 필수입니다");
		}
		chunks = chunks == null ? List.of() : List.copyOf(chunks);
	}

	public int chunkCount() {
		return chunks.size();
	}
}
