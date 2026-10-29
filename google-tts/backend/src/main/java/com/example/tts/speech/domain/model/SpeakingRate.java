package com.example.tts.speech.domain.model;

import com.example.tts.speech.domain.exception.SpeechRuleException;

public record SpeakingRate(double value) {

    public SpeakingRate {
        if (value < 0.25 || value > 4.0) {
            throw new SpeechRuleException("INVALID_RATE", "말하기 속도는 0.25에서 4.0 사이여야 합니다.");
        }
    }
}
