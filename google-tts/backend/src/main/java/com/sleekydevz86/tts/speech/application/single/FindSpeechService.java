package com.sleekydevz86.tts.speech.application.single;

import com.sleekydevz86.tts.speech.application.model.AudioContent;
import com.sleekydevz86.tts.speech.application.policy.StoredAudioNamePolicy;
import com.sleekydevz86.tts.global.exception.TtsException;
import com.sleekydevz86.tts.speech.domain.model.AudioFileName;
import com.sleekydevz86.tts.speech.domain.port.AudioFinder;
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
                .orElseThrow(() -> new TtsException("NOT_FOUND", "파일을 찾을 수 없습니다."));
    }
}
