package com.example.tts.speech.application.multi;

import com.example.tts.speech.domain.model.AudioSummary;

import java.util.List;

@FunctionalInterface
public interface FindSpeechesUseCase {

    List<AudioSummary> execute();
}
