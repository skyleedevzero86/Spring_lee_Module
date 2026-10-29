package com.sleekydevz86.tts.speech.application.delete;

import com.sleekydevz86.tts.speech.application.policy.StoredAudioNamePolicy;
import com.sleekydevz86.tts.global.exception.TtsException;
import com.sleekydevz86.tts.speech.domain.model.AudioFileName;
import com.sleekydevz86.tts.speech.domain.port.AudioDeleter;
import com.sleekydevz86.tts.speech.domain.port.AudioFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeleteSpeechService implements DeleteSpeechUseCase {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final StoredAudioNamePolicy names;
    private final AudioFinder finder;
    private final AudioDeleter deleter;

    public DeleteSpeechService(StoredAudioNamePolicy names, AudioFinder finder, AudioDeleter deleter) {
        this.names = names;
        this.finder = finder;
        this.deleter = deleter;
    }

    @Override
    public void execute(String filename) {
        AudioFileName name = names.require(filename);
        if (!finder.exists(name)) {
            throw new TtsException("NOT_FOUND", "파일을 찾을 수 없습니다.");
        }
        deleter.delete(name);
        log.info("음성을 삭제했습니다. 파일명={}", name.value());
    }
}
