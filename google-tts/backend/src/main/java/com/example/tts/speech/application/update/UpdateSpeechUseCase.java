package com.example.tts.speech.application.update;

import com.example.tts.speech.application.model.AudioContent;

@FunctionalInterface
public interface UpdateSpeechUseCase {

    AudioContent execute(UpdateSpeechCommand command);
}
