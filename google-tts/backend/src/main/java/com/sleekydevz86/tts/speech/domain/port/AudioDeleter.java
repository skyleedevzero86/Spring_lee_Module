package com.sleekydevz86.tts.speech.domain.port;

import com.sleekydevz86.tts.speech.domain.model.AudioFileName;

public interface AudioDeleter {

    void delete(AudioFileName name);
}
