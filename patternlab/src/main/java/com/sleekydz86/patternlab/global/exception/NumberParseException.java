package com.sleekydz86.patternlab.global.exception;

public final class NumberParseException extends RuntimeException {

	public NumberParseException(String message) {
		super(message);
	}

	public NumberParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
