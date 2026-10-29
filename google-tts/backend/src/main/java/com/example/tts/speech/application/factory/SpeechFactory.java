package com.example.tts.speech.application.factory;

import com.example.tts.global.util.AudioFileNameNormalizer;
import com.example.tts.speech.domain.exception.SpeechRuleException;
import com.example.tts.speech.domain.model.AudioFileName;
import com.example.tts.speech.domain.model.Pitch;
import com.example.tts.speech.domain.model.SpeakingRate;
import com.example.tts.speech.domain.model.Speech;
import com.example.tts.speech.domain.model.SpeechText;
import com.example.tts.speech.domain.model.VoiceGender;
import com.example.tts.speech.domain.model.VoiceStyle;
import com.example.tts.speech.domain.service.SpeechLanguageResolver;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SpeechFactory {

    private final AudioFileNameNormalizer normalizer;
    private final SpeechLanguageResolver languages;

    public SpeechFactory(AudioFileNameNormalizer normalizer, SpeechLanguageResolver languages) {
        this.normalizer = normalizer;
        this.languages = languages;
    }

    public Speech assemble(
            String text,
            String filename,
            String language,
            String gender,
            String voiceStyle,
            Double speakingRate,
            Double pitch
    ) {
        return new Speech(
                new SpeechText(text),
                new AudioFileName(normalizer.normalize(filename)),
                languages.resolve(language),
                gender(gender),
                style(voiceStyle),
                new SpeakingRate(speakingRate == null ? 1.0 : speakingRate),
                new Pitch(pitch == null ? 0.0 : pitch)
        );
    }

    private VoiceGender gender(String raw) {
        if (raw == null || raw.isBlank()) {
            return VoiceGender.FEMALE;
        }
        return Arrays.stream(VoiceGender.values())
                .filter(value -> value.name().equalsIgnoreCase(raw.trim()))
                .findFirst()
                .orElseThrow(() -> new SpeechRuleException("INVALID_GENDER", "목소리는 여성 또는 남성이어야 합니다."));
    }

    private VoiceStyle style(String raw) {
        if (raw == null || raw.isBlank()) {
            return VoiceStyle.NEURAL2;
        }
        return Arrays.stream(VoiceStyle.values())
                .filter(value -> value.name().equalsIgnoreCase(raw.trim()))
                .findFirst()
                .orElseThrow(() -> new SpeechRuleException("INVALID_VOICE", "음성 종류는 STANDARD 또는 NEURAL2 이어야 합니다."));
    }
}
