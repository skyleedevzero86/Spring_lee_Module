package com.example.tts.speech.domain.exception;

public class SpeechRuleException extends RuntimeException {

    private final String code;

    public SpeechRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
