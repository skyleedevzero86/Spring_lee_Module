package com.sleekydevz86.tts.speech.application.create;

import com.sleekydevz86.tts.speech.application.factory.SpeechFactory;
import com.sleekydevz86.tts.speech.application.model.AudioContent;
import com.sleekydevz86.tts.speech.domain.model.AudioObject;
import com.sleekydevz86.tts.speech.domain.model.Speech;
import com.sleekydevz86.tts.speech.domain.port.AudioSaver;
import com.sleekydevz86.tts.speech.domain.port.SpeechSynthesizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateSpeechService implements CreateSpeechUseCase {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SpeechFactory factory;
    private final SpeechSynthesizer synthesizer;
    private final AudioSaver saver;

    public CreateSpeechService(SpeechFactory factory, SpeechSynthesizer synthesizer, AudioSaver saver) {
        this.factory = factory;
        this.synthesizer = synthesizer;
        this.saver = saver;
    }

    @Override
    public AudioContent execute(CreateSpeechCommand command) {
        Speech speech = factory.assemble(
                command.text(),
                command.filename(),
                command.language(),
                command.gender(),
                command.voiceStyle(),
                command.speakingRate(),
                command.pitch()
        );
        byte[] content = synthesizer.synthesize(speech);
        saver.save(new AudioObject(speech.fileName(), content));
        log.info("음성을 생성했습니다. 파일명={}", speech.fileName().value());
        return new AudioContent(speech.fileName().value(), content);
    }
}
