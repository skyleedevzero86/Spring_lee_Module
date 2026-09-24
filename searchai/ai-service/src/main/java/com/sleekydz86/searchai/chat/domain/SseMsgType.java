package com.sleekydz86.searchai.chat.domain;

public enum SseMsgType {
	ADD,
	FINISH,
	ROUTING;

	public String eventName() {
		return name().toLowerCase();
	}
}
