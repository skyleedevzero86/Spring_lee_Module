package com.example.tts.speech.application.policy;

import com.example.tts.global.util.AudioFileNameNormalizer;
import com.example.tts.speech.domain.exception.SpeechRuleException;
import com.example.tts.speech.domain.model.AudioFileName;
import org.springframework.stereotype.Component;

@Component
public class StoredAudioNamePolicy {

    private final AudioFileNameNormalizer normalizer;

    public StoredAudioNamePolicy(AudioFileNameNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public AudioFileName require(String raw) {
        if (raw == null || raw.isBlank() || raw.indexOf('/') >= 0 || raw.indexOf('\\') >= 0 || raw.contains("..")) {
            throw new SpeechRuleException("NOT_FOUND", "파일을 찾을 수 없습니다.");
        }
        String normalized = normalizer.normalize(raw);
        if (!normalized.equals(raw.trim())) {
            throw new SpeechRuleException("NOT_FOUND", "파일을 찾을 수 없습니다.");
        }
        return new AudioFileName(normalized);
    }
}
