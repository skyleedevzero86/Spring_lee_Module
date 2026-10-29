package com.example.tts.speech.application.create;

import com.example.tts.speech.application.model.AudioContent;

@FunctionalInterface
public interface CreateSpeechUseCase {

    AudioContent execute(CreateSpeechCommand command);
}
