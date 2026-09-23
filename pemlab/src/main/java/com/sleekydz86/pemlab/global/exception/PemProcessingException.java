package com.sleekydz86.pemlab.global.exception;

public final class PemProcessingException extends RuntimeException {

	public PemProcessingException(String message) {
		super(message);
	}

	public PemProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
}
