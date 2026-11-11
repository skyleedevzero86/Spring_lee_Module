package com.sleekydz86.storagemigration.file.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sleekydz86.storagemigration.file.application.dto.SaveResult;
import com.sleekydz86.storagemigration.file.application.dto.StoredFileResult;
import com.sleekydz86.storagemigration.file.application.dto.UpdateFileDetailCommand;
import com.sleekydz86.storagemigration.file.application.dto.UploadFileCommand;
import com.sleekydz86.storagemigration.file.application.port.out.FileStoragePort;
import com.sleekydz86.storagemigration.file.application.port.out.StoredFileRepository;
import com.sleekydz86.storagemigration.file.domain.FileContent;
import com.sleekydz86.storagemigration.file.domain.FileSearchCondition;
import com.sleekydz86.storagemigration.file.domain.FileStatus;
import com.sleekydz86.storagemigration.file.domain.StorageEntry;
import com.sleekydz86.storagemigration.file.domain.StoredFile;
import com.sleekydz86.storagemigration.global.config.AppProperties;
import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.util.Checksums;
import com.sleekydz86.storagemigration.global.web.Actor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("파일 서비스")
class StoredFileServiceTest {

    private static final String SERVER = "server-1";
    private static final Actor ACTOR = new Actor("tester", "10.0.0.1");

    private FakeRepository repository;
    private FakeStorage storage;
    private StoredFileService service;

    @BeforeEach
    void setUp() {
        repository = new FakeRepository();
        storage = new FakeStorage();
        Clock clock = Clock.fixed(Instant.parse("2026-11-11T00:00:00Z"), ZoneOffset.UTC);
        service = new StoredFileService(repository, storage, clock,
                new AppProperties(Path.of("target", "test-home"), "secret", 1024, List.of()));
    }

    @Test
    @DisplayName("업로드하면 스토리지에 올리고 SHA-256 체크섬과 함께 파일 정보를 등록한다")
    void uploadPutsObjectAndRecordsChecksum() {
        // given
        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);

        // when
        StoredFileResult result = upload("a.txt", bytes);

        // then
        assertThat(storage.objects.get(result.objectKey())).isEqualTo(bytes);
        assertThat(result.checksum()).isEqualTo(Checksums.sha256Hex(bytes));
        assertThat(result.status()).isEqualTo(FileStatus.UPLOADED);
        assertThat(repository.operations).containsExactly("C:tester");
    }

    @Test
    @DisplayName("DB에 저장하면 스토리지 파일을 BLOB으로 옮기고 크기·체크섬으로 검증한다")
    void saveCopiesStorageObjectIntoDatabaseBlob() {
        // given
        byte[] bytes = "report-body".getBytes(StandardCharsets.UTF_8);
        StoredFileResult uploaded = upload("report.pdf", bytes);

        // when
        SaveResult result = service.save(new UpdateFileDetailCommand(uploaded.id(), "보고서", null, List.of("irb")),
                ACTOR);

        // then
        assertThat(repository.contents.get(uploaded.id())).isEqualTo(bytes);
        assertThat(result.verified()).isTrue();
        assertThat(result.databaseBytes()).isEqualTo(bytes.length);
        assertThat(result.file().status()).isEqualTo(FileStatus.SAVED);
        assertThat(result.file().title()).isEqualTo("보고서");
    }

    @Test
    @DisplayName("DB 저장 한도를 넘는 파일은 BLOB으로 저장하지 않는다")
    void saveRejectsFileOverDatabaseLimit() {
        // given
        StoredFileResult uploaded = upload("big.bin", new byte[2048]);

        // when / then
        assertThatThrownBy(() -> service.save(new UpdateFileDetailCommand(uploaded.id(), null, null, null), ACTOR))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.errorCode()).isEqualTo(ErrorCode.FILE_TOO_LARGE));
        assertThat(repository.contents).isEmpty();
    }

    @Test
    @DisplayName("스토리지에서 파일을 지우면 등록된 DB 정보도 함께 지운다")
    void deletingStorageObjectAlsoDeletesDatabaseRecord() {
        // given
        StoredFileResult uploaded = upload("a.txt", new byte[]{1, 2, 3});

        // when
        int deleted = service.deleteStorageObjects(SERVER, List.of(uploaded.objectKey()), ACTOR);

        // then
        assertThat(deleted).isEqualTo(1);
        assertThat(storage.objects).isEmpty();
        assertThat(repository.files).isEmpty();
    }

    @Test
    @DisplayName("DB 정보만 지우도록 고르면 스토리지 파일은 남는다")
    void deleteRecordCanKeepStorageObject() {
        // given
        StoredFileResult uploaded = upload("a.txt", new byte[]{1});

        // when
        service.delete(List.of(uploaded.id()), false, ACTOR);

        // then
        assertThat(repository.files).isEmpty();
        assertThat(storage.objects).containsKey(uploaded.objectKey());
    }

    @Test
    @DisplayName("스토리지 파일이 없어져도 DB에 저장한 BLOB으로 내용을 보여준다")
    void contentFallsBackToDatabaseBlobWhenStorageObjectIsGone() throws IOException {
        // given
        byte[] bytes = "kept-in-db".getBytes(StandardCharsets.UTF_8);
        StoredFileResult uploaded = upload("a.txt", bytes);
        service.save(new UpdateFileDetailCommand(uploaded.id(), null, null, null), ACTOR);
        storage.objects.clear();

        // when
        FileContent content = service.openContent(uploaded.id());

        // then
        assertThat(content.stream().readAllBytes()).isEqualTo(bytes);
    }

    @Test
    @DisplayName("이미 등록한 스토리지 파일은 다시 가져와도 한 번만 등록한다")
    void importSkipsAlreadyRegisteredKeys() {
        // given
        storage.objects.put("legacy/irb/1220/report.pdf", new byte[]{9, 9});

        // when
        List<StoredFileResult> first = service.importFiles(SERVER, List.of("legacy/irb/1220/report.pdf"), ACTOR);
        List<StoredFileResult> second = service.importFiles(SERVER, List.of("legacy/irb/1220/report.pdf"), ACTOR);

        // then
        assertThat(first.getFirst().originalName()).isEqualTo("report.pdf");
        assertThat(second.getFirst().id()).isEqualTo(first.getFirst().id());
        assertThat(repository.files).hasSize(1);
    }

    private StoredFileResult upload(String name, byte[] bytes) {
        return service.upload(new UploadFileCommand(SERVER, name, "application/octet-stream", bytes.length,
                new ByteArrayInputStream(bytes)), ACTOR);
    }

    private static byte[] readAll(InputStream input) {
        try {
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static final class FakeStorage implements FileStoragePort {

        private final Map<String, byte[]> objects = new LinkedHashMap<>();

        @Override
        public String bucketOf(String storageServerId) {
            return "files";
        }

        @Override
        public void put(String storageServerId, String key, InputStream content, long size, String contentType) {
            objects.put(key, readAll(content));
        }

        @Override
        public FileContent open(String storageServerId, String key) {
            byte[] bytes = objects.get(key);
            if (bytes == null) {
                throw new BusinessException(ErrorCode.STORAGE_OBJECT_NOT_FOUND, key);
            }
            return new FileContent(new ByteArrayInputStream(bytes), bytes.length, null, key);
        }

        @Override
        public List<StorageEntry> list(String storageServerId, String prefix) {
            return objects.entrySet().stream()
                    .map(entry -> new StorageEntry(entry.getKey(), entry.getValue().length, null, null))
                    .toList();
        }

        @Override
        public Optional<StorageEntry> stat(String storageServerId, String key) {
            byte[] bytes = objects.get(key);
            return bytes == null ? Optional.empty() : Optional.of(new StorageEntry(key, bytes.length, null, null));
        }

        @Override
        public void delete(String storageServerId, String key, Actor actor) {
            objects.remove(key);
        }
    }

    private static final class FakeRepository implements StoredFileRepository {

        private final Map<String, StoredFile> files = new LinkedHashMap<>();
        private final Map<String, byte[]> contents = new LinkedHashMap<>();
        private final List<String> operations = new ArrayList<>();

        @Override
        public void create(StoredFile file, Actor actor) {
            operations.add("C:" + actor.worker());
            files.put(file.id(), file);
        }

        @Override
        public void update(StoredFile file, Actor actor) {
            operations.add("U:" + actor.worker());
            files.put(file.id(), file);
        }

        @Override
        public void delete(StoredFile file, Actor actor) {
            operations.add("D:" + actor.worker());
            files.remove(file.id());
            contents.remove(file.id());
        }

        @Override
        public void saveContent(String id, InputStream content, long size, Instant savedAt, Actor actor) {
            contents.put(id, readAll(content));
        }

        @Override
        public long contentLength(String id) {
            byte[] bytes = contents.get(id);
            return bytes == null ? 0 : bytes.length;
        }

        @Override
        public Optional<InputStream> openContent(String id) {
            return Optional.ofNullable(contents.get(id)).map(ByteArrayInputStream::new);
        }

        @Override
        public Optional<StoredFile> findById(String id) {
            return Optional.ofNullable(files.get(id));
        }

        @Override
        public List<StoredFile> search(FileSearchCondition condition) {
            return List.copyOf(files.values());
        }

        @Override
        public Map<String, StoredFile> findByObjectKeys(String storageServerId) {
            Map<String, StoredFile> byKey = new LinkedHashMap<>();
            files.values().forEach(file -> byKey.put(file.objectKey(), file));
            return byKey;
        }
    }
}
