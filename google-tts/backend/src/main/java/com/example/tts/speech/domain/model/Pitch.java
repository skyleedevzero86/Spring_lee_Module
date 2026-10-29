package com.example.tts.speech.domain.model;

import com.example.tts.speech.domain.exception.SpeechRuleException;

public record Pitch(double value) {

    public Pitch {
        if (value < -20.0 || value > 20.0) {
            throw new SpeechRuleException("INVALID_PITCH", "음높이는 -20에서 20 사이여야 합니다.");
        }
    }
}
