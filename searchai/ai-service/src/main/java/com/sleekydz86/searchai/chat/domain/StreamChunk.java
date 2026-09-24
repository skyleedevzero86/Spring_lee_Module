package com.sleekydz86.searchai.chat.domain;

public record StreamChunk(String content, SseMsgType type) {

	public static StreamChunk add(String content) {
		return new StreamChunk(content == null ? "" : content, SseMsgType.ADD);
	}

	public static StreamChunk finish(String content) {
		return new StreamChunk(content == null ? "done" : content, SseMsgType.FINISH);
	}

	public static StreamChunk routing(String content) {
		return new StreamChunk(content == null ? "{}" : content, SseMsgType.ROUTING);
	}
}
