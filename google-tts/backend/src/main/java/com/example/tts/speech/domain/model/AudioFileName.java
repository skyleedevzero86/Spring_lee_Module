package com.example.tts.speech.domain.model;

import com.example.tts.speech.domain.exception.SpeechRuleException;

import java.util.Locale;

public record AudioFileName(String value) {

    public AudioFileName {
        if (value == null || value.isBlank() || value.indexOf('/') >= 0 || value.indexOf('\\') >= 0 || value.contains("..")) {
            throw new SpeechRuleException("INVALID_FILENAME", "파일 이름이 올바르지 않습니다.");
        }
        if (!value.toLowerCase(Locale.ROOT).endsWith(".mp3")) {
            throw new SpeechRuleException("INVALID_FILENAME", "파일 이름은 mp3 확장자여야 합니다.");
        }
    }
}
