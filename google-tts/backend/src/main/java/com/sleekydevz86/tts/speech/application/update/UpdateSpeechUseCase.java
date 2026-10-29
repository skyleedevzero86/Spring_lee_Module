package com.sleekydevz86.tts.speech.application.update;

import com.sleekydevz86.tts.speech.application.model.AudioContent;

@FunctionalInterface
public interface UpdateSpeechUseCase {

    AudioContent execute(UpdateSpeechCommand command);
}
