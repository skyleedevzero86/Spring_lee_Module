package com.sleekydevz86.tts.speech.application.single;

import com.sleekydevz86.tts.speech.application.model.AudioContent;

@FunctionalInterface
public interface FindSpeechUseCase {

    AudioContent execute(String filename);
}
