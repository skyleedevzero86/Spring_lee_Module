package com.sleekydz86.searchai.search.domain;

public record SearchHit(
	String title,
	String url,
	String content,
	Double score,
	String engine,
	String category,
	String publishedDate
) {

	public String asContextBlock() {
		return String.format(
			"【출처 제목】: %s%n【내용 요약】: %s%n【링크】: %s",
			nullToEmpty(title),
			nullToEmpty(content),
			nullToEmpty(url)
		);
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
