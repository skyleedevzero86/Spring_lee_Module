package com.example.tts.speech.adapter.out.google;

import com.example.tts.global.config.GoogleCredentialProperties;
import com.example.tts.global.exception.ExternalServiceException;
import com.example.tts.speech.domain.model.Speech;
import com.example.tts.speech.domain.model.VoiceGender;
import com.example.tts.speech.domain.port.SpeechSynthesizer;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class GoogleSpeechSynthesizer implements SpeechSynthesizer {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String cloudPlatform = "https://www.googleapis.com/auth/cloud-platform";
    private final GoogleCredentialProperties properties;
    private final Object lock = new Object();
    private TextToSpeechClient client;

    public GoogleSpeechSynthesizer(GoogleCredentialProperties properties) {
        this.properties = properties;
    }

    @Override
    public byte[] synthesize(Speech speech) {
        SynthesisInput input = SynthesisInput.newBuilder().setText(speech.text().value()).build();
        VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                .setLanguageCode(speech.language().code())
                .setName(speech.language().voiceName(speech.gender(), speech.voiceStyle()))
                .setSsmlGender(toGoogleGender(speech.gender()))
                .build();
        AudioConfig audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.MP3)
                .setSpeakingRate(speech.speakingRate().value())
                .setPitch(speech.pitch().value())
                .build();
        try {
            SynthesizeSpeechResponse response = client().synthesizeSpeech(input, voice, audioConfig);
            return response.getAudioContent().toByteArray();
        } catch (ExternalServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("음성 합성에 실패했습니다.", exception);
            String detail = exception.getMessage() == null ? "원인 불명" : exception.getMessage();
            throw new ExternalServiceException("TTS_FAILED", "음성 합성에 실패했습니다. " + detail);
        }
    }

    @PreDestroy
    public void close() {
        if (client != null) {
            client.close();
        }
    }

    private SsmlVoiceGender toGoogleGender(VoiceGender gender) {
        if (gender == VoiceGender.MALE) {
            return SsmlVoiceGender.MALE;
        }
        return SsmlVoiceGender.FEMALE;
    }

    private TextToSpeechClient client() {
        TextToSpeechClient current = this.client;
        if (current != null) {
            return current;
        }
        synchronized (lock) {
            if (this.client != null) {
                return this.client;
            }
            try {
                TextToSpeechSettings.Builder builder = TextToSpeechSettings.newBuilder();
                GoogleCredentials credentials = loadCredentials();
                if (credentials != null) {
                    builder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
                }
                this.client = TextToSpeechClient.create(builder.build());
                return this.client;
            } catch (ExternalServiceException exception) {
                throw exception;
            } catch (IOException exception) {
                log.error("Google Cloud 자격 증명을 읽지 못했습니다.", exception);
                throw new ExternalServiceException(
                        "CREDENTIALS_MISSING",
                        "Google Cloud 자격 증명을 찾지 못했습니다. service-account-file.json 또는 tts.google.credentials-file 을 설정하세요."
                );
            }
        }
    }

    private GoogleCredentials loadCredentials() throws IOException {
        if (!properties.credentialsFile().isBlank()) {
            Path path = Path.of(properties.credentialsFile());
            if (!Files.isRegularFile(path)) {
                throw new ExternalServiceException("CREDENTIALS_MISSING", "설정한 자격 증명 파일을 찾지 못했습니다.");
            }
            try (InputStream input = Files.newInputStream(path)) {
                return GoogleCredentials.fromStream(input).createScoped(List.of(cloudPlatform));
            }
        }

        ClassPathResource resource = new ClassPathResource("service-account-file.json");
        if (resource.exists()) {
            try (InputStream input = resource.getInputStream()) {
                return GoogleCredentials.fromStream(input).createScoped(List.of(cloudPlatform));
            }
        }

        String applicationDefault = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (applicationDefault != null && !applicationDefault.isBlank()) {
            return null;
        }
        throw new ExternalServiceException(
                "CREDENTIALS_MISSING",
                "Google Cloud 자격 증명을 찾지 못했습니다. service-account-file.json 또는 tts.google.credentials-file 을 설정하세요."
        );
    }
}
