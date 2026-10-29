package com.example.tts.integration.minio;

import com.example.tts.global.config.MinioProperties;
import com.example.tts.speech.adapter.out.minio.MinioAudioStorage;
import com.example.tts.speech.domain.model.AudioFileName;
import com.example.tts.speech.domain.model.AudioObject;
import io.minio.MinioClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MinIO 음성 저장소")
class MinioAudioStorageIntegrationTest {

    @Test
    @DisplayName("실행 중인 MinIO에 음성을 저장하고 조회한 뒤 삭제한다")
    void storesFindsAndDeletesAudio() {
        // given
        MinioProperties properties = new MinioProperties(
                "http://127.0.0.1:9000",
                "minioadmin",
                "minioadmin",
                "tts-audio"
        );
        MinioClient client = MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
        MinioAudioStorage storage = new MinioAudioStorage(client, properties);
        AudioFileName name = new AudioFileName("it-" + UUID.randomUUID() + ".mp3");
        byte[] content = new byte[]{1, 2, 3, 4};

        // when
        storage.save(new AudioObject(name, content));
        byte[] found = storage.findOne(name).orElseThrow().content();
        boolean listed = storage.findMany().stream().anyMatch(summary -> name.value().equals(summary.fileName()));
        storage.delete(name);
        boolean existsAfterDelete = storage.exists(name);

        // then
        assertArrayEquals(content, found);
        assertTrue(listed);
        assertFalse(existsAfterDelete);
    }
}
