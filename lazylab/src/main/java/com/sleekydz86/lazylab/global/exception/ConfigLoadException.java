package com.sleekydz86.lazylab.global.exception;

public final class ConfigLoadException extends RuntimeException {

	public ConfigLoadException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigLoadException(String message) {
		super(message);
	}
}
