package com.example.tts.speech.adapter.in.web;

import com.example.tts.speech.application.create.CreateSpeechCommand;
import com.example.tts.speech.application.create.CreateSpeechUseCase;
import com.example.tts.speech.application.delete.DeleteSpeechUseCase;
import com.example.tts.speech.application.model.AudioContent;
import com.example.tts.speech.application.multi.FindSpeechesUseCase;
import com.example.tts.speech.application.single.FindSpeechUseCase;
import com.example.tts.speech.application.update.UpdateSpeechCommand;
import com.example.tts.speech.application.update.UpdateSpeechUseCase;
import com.example.tts.speech.domain.model.AudioSummary;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/tts")
public class SpeechController {

    private final CreateSpeechUseCase createSpeechUseCase;
    private final UpdateSpeechUseCase updateSpeechUseCase;
    private final DeleteSpeechUseCase deleteSpeechUseCase;
    private final FindSpeechUseCase findSpeechUseCase;
    private final FindSpeechesUseCase findSpeechesUseCase;

    public SpeechController(
            CreateSpeechUseCase createSpeechUseCase,
            UpdateSpeechUseCase updateSpeechUseCase,
            DeleteSpeechUseCase deleteSpeechUseCase,
            FindSpeechUseCase findSpeechUseCase,
            FindSpeechesUseCase findSpeechesUseCase
    ) {
        this.createSpeechUseCase = createSpeechUseCase;
        this.updateSpeechUseCase = updateSpeechUseCase;
        this.deleteSpeechUseCase = deleteSpeechUseCase;
        this.findSpeechUseCase = findSpeechUseCase;
        this.findSpeechesUseCase = findSpeechesUseCase;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> create(@RequestBody SpeechRequest request) {
        AudioContent audio = createSpeechUseCase.execute(new CreateSpeechCommand(
                request.text(),
                request.filename(),
                request.language(),
                request.gender(),
                request.voiceStyle(),
                request.speakingRate(),
                request.pitch()
        ));
        return audioResponse(audio);
    }

    @PutMapping(value = "/files/{filename:.+}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> update(@PathVariable String filename, @RequestBody SpeechRequest request) {
        AudioContent audio = updateSpeechUseCase.execute(new UpdateSpeechCommand(
                request.text(),
                filename,
                request.language(),
                request.gender(),
                request.voiceStyle(),
                request.speakingRate(),
                request.pitch()
        ));
        return audioResponse(audio);
    }

    @DeleteMapping("/files/{filename:.+}")
    public ResponseEntity<Void> delete(@PathVariable String filename) {
        deleteSpeechUseCase.execute(filename);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<byte[]> findOne(@PathVariable String filename) {
        return audioResponse(findSpeechUseCase.execute(filename));
    }

    @GetMapping("/files")
    public List<SavedAudioResponse> findMany() {
        return findSpeechesUseCase.execute().stream()
                .map(this::toResponse)
                .toList();
    }

    private SavedAudioResponse toResponse(AudioSummary summary) {
        return new SavedAudioResponse(summary.fileName(), summary.size(), summary.modifiedAt());
    }

    private ResponseEntity<byte[]> audioResponse(AudioContent audio) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(audio.fileName(), StandardCharsets.UTF_8)
                .build();
        String encodedName = URLEncoder.encode(audio.fileName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Audio-Filename", encodedName)
                .body(audio.content());
    }
}
