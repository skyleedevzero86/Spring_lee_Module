package com.sleekydz86.jvmperf.global.exception;

public final class MemoryLoadException extends RuntimeException {

	public MemoryLoadException(String message, Throwable cause) {
		super(message, cause);
	}

	public MemoryLoadException(String message) {
		super(message);
	}
}
