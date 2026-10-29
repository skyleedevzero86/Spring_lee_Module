package com.sleekydevz86.tts.speech.domain.port;

import com.sleekydevz86.tts.speech.domain.model.Speech;

public interface SpeechSynthesizer {

    byte[] synthesize(Speech speech);
}
