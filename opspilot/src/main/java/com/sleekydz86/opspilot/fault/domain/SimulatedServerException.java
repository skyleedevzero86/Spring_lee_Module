package com.sleekydz86.opspilot.fault.domain;

public final class SimulatedServerException extends RuntimeException {

	private final String code;

	public SimulatedServerException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String code() {
		return code;
	}
}
