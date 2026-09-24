package com.sleekydz86.searchai.global.splitter;

import com.sleekydz86.searchai.knowledge.application.port.out.TextSplitStrategy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public final class CustomTextSplitter implements TextSplitStrategy {

	@Override
	public List<String> split(String text) {
		if (text == null || text.isBlank()) {
			return List.of();
		}
		return Arrays.stream(text.split("\\s*\\R\\s*\\R\\S*"))
			.map(String::trim)
			.filter(part -> !part.isBlank())
			.toList();
	}
}
