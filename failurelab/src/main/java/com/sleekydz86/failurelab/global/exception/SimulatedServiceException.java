package com.sleekydz86.failurelab.global.exception;

public abstract class SimulatedServiceException extends RuntimeException {

	protected SimulatedServiceException(String message) {
		super(message);
	}
}
