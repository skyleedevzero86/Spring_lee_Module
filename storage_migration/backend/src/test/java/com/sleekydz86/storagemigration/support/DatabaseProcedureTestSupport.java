package com.sleekydz86.storagemigration.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sleekydz86.storagemigration.audit.adapter.out.persistence.MyBatisAuditLogRepository;
import com.sleekydz86.storagemigration.audit.domain.AuditLog;
import com.sleekydz86.storagemigration.audit.domain.AuditSearchCondition;
import com.sleekydz86.storagemigration.file.adapter.out.persistence.MyBatisStoredFileRepository;
import com.sleekydz86.storagemigration.file.domain.FileSearchCondition;
import com.sleekydz86.storagemigration.file.domain.FileStatus;
import com.sleekydz86.storagemigration.file.domain.StoredFile;
import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.jdbc.ActiveDatabase;
import com.sleekydz86.storagemigration.global.jdbc.SchemaInitializer;
import com.sleekydz86.storagemigration.global.jdbc.SqlDialect;
import com.sleekydz86.storagemigration.global.mybatis.MyBatisSessions;
import com.sleekydz86.storagemigration.global.util.Checksums;
import com.sleekydz86.storagemigration.global.util.SecretCipher;
import com.sleekydz86.storagemigration.global.web.Actor;
import com.sleekydz86.storagemigration.storage.adapter.out.persistence.MyBatisStorageServerRepository;
import com.sleekydz86.storagemigration.storage.domain.StorageServer;
import com.sleekydz86.storagemigration.storage.domain.StorageStatus;
import com.sleekydz86.storagemigration.storage.domain.StorageType;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public abstract class DatabaseProcedureTestSupport {

    private static final Actor ACTOR = new Actor("통합테스트", "10.0.0.7");

    private ActiveDatabase database;
    private MyBatisStorageServerRepository servers;
    private MyBatisStoredFileRepository files;
    private MyBatisAuditLogRepository auditLogs;

    protected abstract SqlDialect dialect();

    protected abstract String prefix();

    @BeforeEach
    void connect() {
        database = new ActiveDatabase(new SchemaInitializer());
        database.switchTo("it", dialect(), System.getenv(prefix() + "_URL"), System.getenv(prefix() + "_USER"),
                System.getenv(prefix() + "_PASSWORD"));
        MyBatisSessions sessions = new MyBatisSessions(database);
        servers = new MyBatisStorageServerRepository(sessions, SecretCipher.withSecret("it-secret"));
        files = new MyBatisStoredFileRepository(sessions);
        auditLogs = new MyBatisAuditLogRepository(sessions);
    }

    @AfterEach
    void disconnect() {
        database.disconnect();
    }

    @Test
    @DisplayName("스토리지 서버를 프로시저로 등록·수정하고, 키는 암호화된 채 저장된다")
    void storageServerProcedure() {
        // given
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        StorageServer server = StorageServer.register(UUID.randomUUID().toString(), "IT 스토리지",
                StorageType.MINIO_CONSOLE, "http://localhost:9001", null, "storage-migration-it", "access-it",
                "secret-it", now);

        // when
        servers.create(server, ACTOR);
        server.recordSuccess("연결 확인", now);
        servers.update(server, ACTOR);

        // then
        StorageServer saved = servers.findById(server.id()).orElseThrow();
        assertThat(saved.secretKey()).isEqualTo("secret-it");
        assertThat(saved.lastMessage()).isEqualTo("연결 확인");
        assertThat(auditActions(server.id())).contains("STORAGE_SERVER_C", "STORAGE_SERVER_U");

        servers.delete(saved, ACTOR);
        assertThat(servers.findById(server.id())).isEmpty();
    }

    @Test
    @DisplayName("다른 암호화 키로 저장된 서버가 있어도 목록은 깨지지 않고 다시 입력하라고 알려준다")
    void unreadableKeysDoNotBreakList() {
        // given
        StorageServer server = StorageServer.register(UUID.randomUUID().toString(), "다른 키 스토리지",
                StorageType.MINIO, "http://localhost:9000", null, "storage-migration-it", "access", "secret",
                Instant.now());
        servers.create(server, ACTOR);
        MyBatisStorageServerRepository otherKey = new MyBatisStorageServerRepository(new MyBatisSessions(database),
                SecretCipher.withSecret("another-secret"));

        // when
        List<StorageServer> listed = otherKey.findAll();

        // then
        StorageServer unreadable = listed.stream().filter(item -> item.id().equals(server.id())).findFirst().orElseThrow();
        assertThat(unreadable.status()).isEqualTo(StorageStatus.FAILED);
        assertThat(unreadable.lastMessage()).isEqualTo(MyBatisStorageServerRepository.UNREADABLE_KEYS);

        servers.delete(servers.findById(server.id()).orElseThrow(), ACTOR);
    }

    @Test
    @DisplayName("파일 정보와 파일 자체(BLOB)를 저장하고, 크기·내용·작업 로그를 확인한다")
    void storedFileWithBlob() throws Exception {
        // given
        byte[] bytes = "IRB 1220 보고서 본문".repeat(500).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        StoredFile file = StoredFile.uploaded(UUID.randomUUID().toString(), "it-server", "storage-migration-it",
                "legacy/irb/1220/report.pdf", "report.pdf", "application/pdf", bytes.length,
                Checksums.sha256Hex(bytes), Instant.now());
        files.create(file, ACTOR);

        // when
        files.saveContent(file.id(), new ByteArrayInputStream(bytes), bytes.length, Instant.now(), ACTOR);
        file.markSaved(Checksums.sha256Hex(bytes), Instant.now());
        file.updateDetail("IRB 보고서", "이관 테스트", List.of("irb", "1220"), Instant.now());
        files.update(file, ACTOR);

        // then
        assertThat(files.contentLength(file.id())).isEqualTo(bytes.length);
        try (InputStream input = files.openContent(file.id()).orElseThrow()) {
            assertThat(Checksums.sha256Hex(input.readAllBytes())).isEqualTo(Checksums.sha256Hex(bytes));
        }
        StoredFile saved = files.findById(file.id()).orElseThrow();
        assertThat(saved.status()).isEqualTo(FileStatus.SAVED);
        assertThat(saved.worker()).isEqualTo("통합테스트");
        assertThat(saved.workIp()).isEqualTo("10.0.0.7");
        assertThat(files.search(new FileSearchCondition("irb 보고서", null, FileStatus.SAVED)))
                .extracting(StoredFile::id).contains(file.id());
        assertThat(auditActions(file.id())).contains("STORED_FILE_C", "STORED_FILE_SAVE", "STORED_FILE_U");

        files.delete(saved, ACTOR);
        assertThat(files.findById(file.id())).isEmpty();
        assertThat(auditActions(file.id())).contains("STORED_FILE_D");
    }

    @Test
    @DisplayName("없는 파일을 수정하면 프로시저가 돌려준 한국어 err_msg로 실패한다")
    void procedureReturnsKoreanError() {
        // given
        StoredFile missing = StoredFile.uploaded(UUID.randomUUID().toString(), "it-server", "b", "k", "none.txt",
                null, 1, null, Instant.now());

        // when / then
        assertThatThrownBy(() -> files.update(missing, ACTOR))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("SP_SM_STORED_FILE_CUD")
                .hasMessageContaining("파일 정보를 찾을 수 없습니다");
    }

    private List<String> auditActions(String targetId) {
        return auditLogs.search(new AuditSearchCondition(null, targetId, null, 50)).stream()
                .peek(log -> assertThat(log.worker()).isEqualTo("통합테스트"))
                .map(AuditLog::action)
                .toList();
    }
}
