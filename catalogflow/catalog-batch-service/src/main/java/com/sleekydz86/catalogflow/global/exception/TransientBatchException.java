package com.sleekydz86.catalogflow.global.exception;

public class TransientBatchException extends ApplicationException {

	public TransientBatchException(String message) {
		super(message);
	}

	public TransientBatchException(String message, Throwable cause) {
		super(message, cause);
	}
}
