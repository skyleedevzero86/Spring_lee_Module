package com.sleekydevz86.tts.speech.domain.port;

import com.sleekydevz86.tts.speech.domain.model.AudioObject;

public interface AudioSaver {

    void save(AudioObject audio);
}
