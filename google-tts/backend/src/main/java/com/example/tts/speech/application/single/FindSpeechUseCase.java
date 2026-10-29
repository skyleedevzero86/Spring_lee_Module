package com.example.tts.speech.application.single;

import com.example.tts.speech.application.model.AudioContent;

@FunctionalInterface
public interface FindSpeechUseCase {

    AudioContent execute(String filename);
}
