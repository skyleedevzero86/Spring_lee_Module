package com.sleekydz86.opspilot.global.exception;

public final class ExternalServiceException extends DomainException {

	public ExternalServiceException(String message) {
		super(message);
	}

	public ExternalServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
