package com.sleekydz86.searchai.chat.domain;

public record StreamChunk(
	String content,
	SseMsgType type,
	String requestId,
	long sequence,
	String finishReason
) {

	public static StreamChunk add(String content) {
		return new StreamChunk(content == null ? "" : content, SseMsgType.ADD, "", 0, "");
	}

	public static StreamChunk add(String content, String requestId, long sequence) {
		return new StreamChunk(content == null ? "" : content, SseMsgType.ADD, nullToEmpty(requestId), sequence, "");
	}

	public static StreamChunk finish(String content) {
		return new StreamChunk(content == null ? "done" : content, SseMsgType.FINISH, "", 0, content == null ? "done" : content);
	}

	public static StreamChunk finish(String content, String requestId, long sequence, String finishReason) {
		String reason = finishReason == null || finishReason.isBlank() ? "done" : finishReason;
		return new StreamChunk(content == null ? reason : content, SseMsgType.FINISH, nullToEmpty(requestId), sequence, reason);
	}

	public static StreamChunk routing(String content) {
		return new StreamChunk(content == null ? "{}" : content, SseMsgType.ROUTING, "", 0, "");
	}

	public static StreamChunk routing(String content, String requestId, long sequence) {
		return new StreamChunk(content == null ? "{}" : content, SseMsgType.ROUTING, nullToEmpty(requestId), sequence, "");
	}

	public StreamChunk withMeta(String requestId, long sequence) {
		return new StreamChunk(content, type, nullToEmpty(requestId), sequence, finishReason);
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
