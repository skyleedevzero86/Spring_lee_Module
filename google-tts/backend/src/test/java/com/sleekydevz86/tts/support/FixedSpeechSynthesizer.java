package com.sleekydevz86.tts.support;

import com.sleekydevz86.tts.speech.domain.model.Speech;
import com.sleekydevz86.tts.speech.domain.port.SpeechSynthesizer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class FixedSpeechSynthesizer implements SpeechSynthesizer {

    private final List<Speech> requests = new ArrayList<>();

    @Override
    public byte[] synthesize(Speech speech) {
        requests.add(speech);
        return speech.text().value().getBytes(StandardCharsets.UTF_8);
    }

    public List<Speech> requests() {
        return List.copyOf(requests);
    }
}
