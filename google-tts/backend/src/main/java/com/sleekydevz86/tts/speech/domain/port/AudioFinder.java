package com.sleekydevz86.tts.speech.domain.port;

import com.sleekydevz86.tts.speech.domain.model.AudioFileName;
import com.sleekydevz86.tts.speech.domain.model.AudioObject;
import com.sleekydevz86.tts.speech.domain.model.AudioSummary;

import java.util.List;
import java.util.Optional;

public interface AudioFinder {

    Optional<AudioObject> findOne(AudioFileName name);

    List<AudioSummary> findMany();

    boolean exists(AudioFileName name);
}
