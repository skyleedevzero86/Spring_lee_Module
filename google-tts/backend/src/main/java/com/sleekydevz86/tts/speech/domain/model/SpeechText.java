package com.sleekydevz86.tts.speech.domain.model;

import com.sleekydevz86.tts.global.exception.TtsException;

import java.nio.charset.StandardCharsets;

public record SpeechText(String value) {

    public SpeechText {
        if (value == null || value.isBlank()) {
            throw new TtsException("INVALID_TEXT", "텍스트를 입력해야 합니다.");
        }
        value = value.trim();
        if (value.getBytes(StandardCharsets.UTF_8).length > 5000) {
            throw new TtsException("INVALID_TEXT", "텍스트는 5000바이트를 넘을 수 없습니다.");
        }
    }
}
