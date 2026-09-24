package com.sleekydz86.searchai.gateway.global.web;

public record LeeResult<T>(int status, String msg, T data) {

	public static <T> LeeResult<T> ok(String msg) {
		return new LeeResult<>(200, msg, null);
	}

	public static <T> LeeResult<T> error(String msg) {
		return new LeeResult<>(500, msg, null);
	}
}
