package com.sleekydz86.storagemigration.storage.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sleekydz86.storagemigration.global.error.BusinessException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("스토리지 서버")
class StorageServerTest {

    private static final Instant NOW = Instant.parse("2026-11-11T00:00:00Z");

    @Test
    @DisplayName("MinIO는 접속 주소가 없으면 등록할 수 없다")
    void minioNeedsEndpoint() {
        // given
        String endpoint = null;

        // when / then
        assertThatThrownBy(() -> StorageServer.register("1", "local", StorageType.MINIO, endpoint, null, "files", "id",
                "pw", NOW)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("MinIO 콘솔(9001)은 주소 끝의 슬래시를 지우고 미확인 상태로 시작한다")
    void minioConsoleTrimsEndpoint() {
        // given
        String endpoint = "http://localhost:9001/";

        // when
        StorageServer server = StorageServer.register("1", "console", StorageType.MINIO_CONSOLE, endpoint, null,
                "files", "id", "pw", NOW);

        // then
        assertThat(server.endpoint()).isEqualTo("http://localhost:9001");
        assertThat(server.region()).isEqualTo(StorageServer.DEFAULT_REGION);
        assertThat(server.status()).isEqualTo(StorageStatus.UNTESTED);
    }

    @Test
    @DisplayName("S3는 리전이 필요하고 경로 방식(path-style)을 쓰지 않는다")
    void s3NeedsRegion() {
        // given
        String region = "ap-northeast-2";

        // when
        StorageServer server = StorageServer.register("1", "aws", StorageType.S3, null, region, "files", "id", "pw",
                NOW);

        // then
        assertThat(server.pathStyleAccess()).isFalse();
        assertThat(server.hasCustomEndpoint()).isFalse();
        assertThatThrownBy(() -> StorageServer.register("1", "aws", StorageType.S3, null, null, "files", "id", "pw",
                NOW)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("버킷 이름 규칙에 맞지 않으면 거부한다")
    void rejectsInvalidBucketName() {
        // given
        String bucket = "Bad_Bucket";

        // when / then
        assertThatThrownBy(() -> StorageServer.register("1", "local", StorageType.MINIO, "http://localhost:9000",
                null, bucket, "id", "pw", NOW)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("수정할 때 아이디·비밀번호를 비우면 기존 값을 유지하고 다시 테스트해야 한다")
    void blankKeysKeepExistingValues() {
        // given
        StorageServer server = StorageServer.register("1", "local", StorageType.MINIO, "http://localhost:9000", null,
                "files", "id", "pw", NOW);
        server.recordSuccess("ok", NOW);

        // when
        server.changeSettings("local2", StorageType.MINIO, "http://localhost:9000", null, "files", "", "",
                NOW.plusSeconds(1));

        // then
        assertThat(server.secretKey()).isEqualTo("pw");
        assertThat(server.accessKey()).isEqualTo("id");
        assertThat(server.status()).isEqualTo(StorageStatus.UNTESTED);
    }
}
