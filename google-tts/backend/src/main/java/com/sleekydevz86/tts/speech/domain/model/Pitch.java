package com.sleekydevz86.tts.speech.domain.model;

import com.sleekydevz86.tts.global.exception.TtsException;

public record Pitch(double value) {

    public Pitch {
        if (value < -20.0 || value > 20.0) {
            throw new TtsException("INVALID_PITCH", "음높이는 -20에서 20 사이여야 합니다.");
        }
    }
}
