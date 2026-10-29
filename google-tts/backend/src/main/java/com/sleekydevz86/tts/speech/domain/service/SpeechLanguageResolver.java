package com.sleekydevz86.tts.speech.domain.service;

import com.sleekydevz86.tts.global.exception.TtsException;
import com.sleekydevz86.tts.speech.domain.model.SpeechLanguage;

import java.util.Arrays;

public class SpeechLanguageResolver {

    public SpeechLanguage resolve(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new TtsException("INVALID_LANGUAGE", "언어를 입력해야 합니다.");
        }
        String code = raw.trim();
        return Arrays.stream(SpeechLanguage.values())
                .filter(language -> language.code().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new TtsException(
                        "INVALID_LANGUAGE",
                        "지원하지 않는 언어입니다. ko-KR, ja-JP, cmn-CN, en-US 중 하나여야 합니다."
                ));
    }
}
