package com.sleekydz86.searchai.usage.domain;

public enum RequestType {
	FAQ,
	DB,
	RAG,
	GENERAL,
	SUMMARY,
	CODE,
	WEB;

	public boolean requiresExternalLlm() {
		return this == GENERAL || this == SUMMARY || this == CODE || this == WEB || this == RAG;
	}

	public boolean isInternalOnly() {
		return this == FAQ || this == DB;
	}
}
