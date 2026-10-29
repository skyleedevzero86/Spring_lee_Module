package com.example.tts.speech.application.single;

import com.example.tts.speech.application.model.AudioContent;
import com.example.tts.speech.application.policy.StoredAudioNamePolicy;
import com.example.tts.speech.domain.exception.SpeechRuleException;
import com.example.tts.speech.domain.model.AudioFileName;
import com.example.tts.speech.domain.port.AudioFinder;
import org.springframework.stereotype.Service;

@Service
public class FindSpeechService implements FindSpeechUseCase {

    private final StoredAudioNamePolicy names;
    private final AudioFinder finder;

    public FindSpeechService(StoredAudioNamePolicy names, AudioFinder finder) {
        this.names = names;
        this.finder = finder;
    }

    @Override
    public AudioContent execute(String filename) {
        AudioFileName name = names.require(filename);
        return finder.findOne(name)
                .map(audio -> new AudioContent(audio.name().value(), audio.content()))
                .orElseThrow(() -> new SpeechRuleException("NOT_FOUND", "파일을 찾을 수 없습니다."));
    }
}
