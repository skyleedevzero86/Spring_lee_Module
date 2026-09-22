package com.sleekydz86.concurrentlab.global.exception;

public final class LookupFailedException extends RuntimeException {

	public LookupFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
