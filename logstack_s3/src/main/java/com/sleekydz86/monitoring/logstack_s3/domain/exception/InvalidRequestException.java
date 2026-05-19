package com.sleekydz86.monitoring.logstack_s3.domain.exception;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
