package com.example.tts.global.exception;

public class ExternalServiceException extends RuntimeException {

    private final String code;

    public ExternalServiceException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
