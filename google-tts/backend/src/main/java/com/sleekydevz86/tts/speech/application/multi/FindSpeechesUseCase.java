package com.sleekydevz86.tts.speech.application.multi;

import com.sleekydevz86.tts.speech.domain.model.AudioSummary;

import java.util.List;

@FunctionalInterface
public interface FindSpeechesUseCase {

    List<AudioSummary> execute();
}
