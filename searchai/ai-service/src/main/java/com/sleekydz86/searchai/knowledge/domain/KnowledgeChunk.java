package com.sleekydz86.searchai.knowledge.domain;

import java.util.Map;

public record KnowledgeChunk(String text, Map<String, Object> metadata) {

	public KnowledgeChunk {
		text = text == null ? "" : text;
		metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
	}

	public String fileName() {
		Object value = metadata.get("fileName");
		return value == null ? "알 수 없는 문서" : value.toString();
	}
}
