package com.sleekydz86.storagemigration.storage.application.dto;

import com.sleekydz86.storagemigration.global.util.Texts;
import com.sleekydz86.storagemigration.storage.domain.StorageServer;
import com.sleekydz86.storagemigration.storage.domain.StorageStatus;
import com.sleekydz86.storagemigration.storage.domain.StorageType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "스토리지 서버 정보 (비밀번호는 돌려주지 않음)")
public record StorageServerResult(
        @Schema(description = "스토리지 서버 ID (UUID)") String id,
        @Schema(description = "표시 이름") String name,
        @Schema(description = "종류") StorageType type,
        @Schema(description = "접속 주소") String endpoint,
        @Schema(description = "리전") String region,
        @Schema(description = "버킷 이름") String bucket,
        @Schema(description = "가린 아이디") String maskedAccessKey,
        @Schema(description = "연결 상태 (UNTESTED 미확인, CONNECTED 연결됨, FAILED 실패)") StorageStatus status,
        @Schema(description = "마지막 연결 테스트 결과 메시지") String lastMessage,
        @Schema(description = "마지막 연결 테스트 일시") Instant lastCheckedAt,
        @Schema(description = "등록 일시") Instant createdAt,
        @Schema(description = "수정 일시") Instant updatedAt) {

    public static StorageServerResult from(StorageServer server) {
        return new StorageServerResult(server.id(), server.name(), server.type(), server.endpoint(), server.region(),
                server.bucket(), Texts.mask(server.accessKey()), server.status(), server.lastMessage(),
                server.lastCheckedAt(), server.createdAt(), server.updatedAt());
    }
}
