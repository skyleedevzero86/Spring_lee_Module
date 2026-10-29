package com.sleekydevz86.tts.global.exception;

public class TtsException extends RuntimeException {

    private final String code;

    public TtsException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
