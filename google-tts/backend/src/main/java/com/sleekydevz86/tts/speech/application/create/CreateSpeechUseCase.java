package com.sleekydevz86.tts.speech.application.create;

import com.sleekydevz86.tts.speech.application.model.AudioContent;

@FunctionalInterface
public interface CreateSpeechUseCase {

    AudioContent execute(CreateSpeechCommand command);
}
