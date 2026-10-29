package com.sleekydevz86.tts.speech.application.policy;

import com.sleekydevz86.tts.global.util.AudioFileNameNormalizer;
import com.sleekydevz86.tts.global.exception.TtsException;
import com.sleekydevz86.tts.speech.domain.model.AudioFileName;
import org.springframework.stereotype.Component;

@Component
public class StoredAudioNamePolicy {

    private final AudioFileNameNormalizer normalizer;

    public StoredAudioNamePolicy(AudioFileNameNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public AudioFileName require(String raw) {
        if (raw == null || raw.isBlank() || raw.indexOf('/') >= 0 || raw.indexOf('\\') >= 0 || raw.contains("..")) {
            throw new TtsException("NOT_FOUND", "파일을 찾을 수 없습니다.");
        }
        String normalized = normalizer.normalize(raw);
        if (!normalized.equals(raw.trim())) {
            throw new TtsException("NOT_FOUND", "파일을 찾을 수 없습니다.");
        }
        return new AudioFileName(normalized);
    }
}
