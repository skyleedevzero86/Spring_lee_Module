package com.sleekydz86.catalogflow.eventcontract;

public class IntegrationEventParseException extends RuntimeException {

	public IntegrationEventParseException(String message) {
		super(message);
	}

	public IntegrationEventParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
