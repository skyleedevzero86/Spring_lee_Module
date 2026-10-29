package com.example.tts.speech.adapter.out.minio;

import com.example.tts.global.config.MinioProperties;
import com.example.tts.global.exception.ExternalServiceException;
import com.example.tts.speech.domain.model.AudioFileName;
import com.example.tts.speech.domain.model.AudioObject;
import com.example.tts.speech.domain.model.AudioSummary;
import com.example.tts.speech.domain.port.AudioDeleter;
import com.example.tts.speech.domain.port.AudioFinder;
import com.example.tts.speech.domain.port.AudioSaver;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Component
public class MinioAudioStorage implements AudioSaver, AudioFinder, AudioDeleter {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MinioClient client;
    private final MinioProperties properties;

    public MinioAudioStorage(MinioClient client, MinioProperties properties) {
        this.client = client;
        this.properties = properties;
        ensureBucket();
    }

    @Override
    public void save(AudioObject audio) {
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(audio.name().value())
                    .stream(new ByteArrayInputStream(audio.content()), audio.content().length, -1)
                    .contentType("audio/mpeg")
                    .build());
            log.info("음성 파일을 저장소에 저장했습니다. 파일명={}", audio.name().value());
        } catch (Exception exception) {
            throw storageFailure("음성 파일을 저장하지 못했습니다.", exception);
        }
    }

    @Override
    public Optional<AudioObject> findOne(AudioFileName name) {
        try (InputStream input = client.getObject(GetObjectArgs.builder()
                .bucket(properties.bucket())
                .object(name.value())
                .build())) {
            return Optional.of(new AudioObject(name, input.readAllBytes()));
        } catch (ErrorResponseException exception) {
            if (missing(exception)) {
                return Optional.empty();
            }
            throw storageFailure("음성 파일을 읽지 못했습니다.", exception);
        } catch (Exception exception) {
            throw storageFailure("음성 파일을 읽지 못했습니다.", exception);
        }
    }

    @Override
    public List<AudioSummary> findMany() {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(properties.bucket())
                .recursive(true)
                .build());
        return StreamSupport.stream(results.spliterator(), false)
                .map(this::readItem)
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(AudioSummary::modifiedAt).reversed())
                .toList();
    }

    @Override
    public boolean exists(AudioFileName name) {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(name.value())
                    .build());
            return true;
        } catch (ErrorResponseException exception) {
            if (missing(exception)) {
                return false;
            }
            throw storageFailure("음성 파일 존재 여부를 확인하지 못했습니다.", exception);
        } catch (Exception exception) {
            throw storageFailure("음성 파일 존재 여부를 확인하지 못했습니다.", exception);
        }
    }

    @Override
    public void delete(AudioFileName name) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(name.value())
                    .build());
            log.info("음성 파일을 저장소에서 삭제했습니다. 파일명={}", name.value());
        } catch (Exception exception) {
            throw storageFailure("음성 파일을 삭제하지 못했습니다.", exception);
        }
    }

    private void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(properties.bucket()).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(properties.bucket()).build());
                log.info("MinIO 버킷을 생성했습니다. bucket={}", properties.bucket());
            }
        } catch (Exception exception) {
            throw storageFailure("음성 저장소 버킷을 준비하지 못했습니다.", exception);
        }
    }

    private Optional<AudioSummary> readItem(Result<Item> result) {
        try {
            Item item = result.get();
            String name = item.objectName();
            if (item.isDir() || name == null || !name.toLowerCase(Locale.ROOT).endsWith(".mp3")) {
                return Optional.empty();
            }
            return Optional.of(new AudioSummary(name, item.size(), modifiedAt(item)));
        } catch (Exception exception) {
            log.warn("저장소 항목을 읽지 못했습니다.", exception);
            return Optional.empty();
        }
    }

    private Instant modifiedAt(Item item) {
        ZonedDateTime modified = item.lastModified();
        if (modified == null) {
            return Instant.EPOCH;
        }
        return modified.toInstant();
    }

    private boolean missing(ErrorResponseException exception) {
        return "NoSuchKey".equals(exception.errorResponse().code());
    }

    private ExternalServiceException storageFailure(String message, Exception exception) {
        log.error(message, exception);
        return new ExternalServiceException("TTS_FAILED", message);
    }
}
