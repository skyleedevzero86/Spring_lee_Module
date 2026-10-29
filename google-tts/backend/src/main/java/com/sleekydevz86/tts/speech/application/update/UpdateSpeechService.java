package com.sleekydevz86.tts.speech.application.update;

import com.sleekydevz86.tts.speech.application.factory.SpeechFactory;
import com.sleekydevz86.tts.speech.application.model.AudioContent;
import com.sleekydevz86.tts.speech.application.policy.StoredAudioNamePolicy;
import com.sleekydevz86.tts.global.exception.TtsException;
import com.sleekydevz86.tts.speech.domain.model.AudioFileName;
import com.sleekydevz86.tts.speech.domain.model.AudioObject;
import com.sleekydevz86.tts.speech.domain.model.Speech;
import com.sleekydevz86.tts.speech.domain.port.AudioFinder;
import com.sleekydevz86.tts.speech.domain.port.AudioSaver;
import com.sleekydevz86.tts.speech.domain.port.SpeechSynthesizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UpdateSpeechService implements UpdateSpeechUseCase {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SpeechFactory factory;
    private final StoredAudioNamePolicy names;
    private final AudioFinder finder;
    private final SpeechSynthesizer synthesizer;
    private final AudioSaver saver;

    public UpdateSpeechService(
            SpeechFactory factory,
            StoredAudioNamePolicy names,
            AudioFinder finder,
            SpeechSynthesizer synthesizer,
            AudioSaver saver
    ) {
        this.factory = factory;
        this.names = names;
        this.finder = finder;
        this.synthesizer = synthesizer;
        this.saver = saver;
    }

    @Override
    public AudioContent execute(UpdateSpeechCommand command) {
        AudioFileName name = names.require(command.filename());
        if (!finder.exists(name)) {
            throw new TtsException("NOT_FOUND", "수정할 파일을 찾을 수 없습니다.");
        }
        Speech speech = factory.assemble(
                command.text(),
                name.value(),
                command.language(),
                command.gender(),
                command.voiceStyle(),
                command.speakingRate(),
                command.pitch()
        );
        byte[] content = synthesizer.synthesize(speech);
        saver.save(new AudioObject(speech.fileName(), content));
        log.info("음성을 수정했습니다. 파일명={}", speech.fileName().value());
        return new AudioContent(speech.fileName().value(), content);
    }
}
