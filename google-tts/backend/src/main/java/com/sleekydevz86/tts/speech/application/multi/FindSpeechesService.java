package com.sleekydevz86.tts.speech.application.multi;

import com.sleekydevz86.tts.speech.domain.model.AudioSummary;
import com.sleekydevz86.tts.speech.domain.port.AudioFinder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FindSpeechesService implements FindSpeechesUseCase {

    private final AudioFinder finder;

    public FindSpeechesService(AudioFinder finder) {
        this.finder = finder;
    }

    @Override
    public List<AudioSummary> execute() {
        return finder.findMany();
    }
}
