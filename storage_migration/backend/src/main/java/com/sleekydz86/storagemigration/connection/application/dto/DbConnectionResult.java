package com.sleekydz86.storagemigration.connection.application.dto;

import com.sleekydz86.storagemigration.connection.domain.ConnectionStatus;
import com.sleekydz86.storagemigration.connection.domain.DbConnection;
import com.sleekydz86.storagemigration.connection.domain.DbVendor;
import com.sleekydz86.storagemigration.global.util.Texts;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "DB 연결 정보 (비밀번호는 돌려주지 않음)")
public record DbConnectionResult(
        @Schema(description = "DB 연결 ID (UUID)") String id,
        @Schema(description = "표시 이름") String name,
        @Schema(description = "DB 종류") DbVendor vendor,
        @Schema(description = "호스트") String host,
        @Schema(description = "포트") int port,
        @Schema(description = "DB 이름 (Oracle은 서비스 이름)") String database,
        @Schema(description = "가린 아이디") String maskedUsername,
        @Schema(description = "연결 상태 (UNTESTED 미확인, CONNECTED 연결됨, FAILED 실패)") ConnectionStatus status,
        @Schema(description = "마지막 연결 테스트 결과 메시지") String lastMessage,
        @Schema(description = "마지막 연결 테스트 일시") Instant lastCheckedAt,
        @Schema(description = "지금 사용 중인 DB인지 여부") boolean active,
        @Schema(description = "등록 일시") Instant createdAt,
        @Schema(description = "수정 일시") Instant updatedAt) {

    public static DbConnectionResult from(DbConnection connection) {
        return new DbConnectionResult(connection.id(), connection.name(), connection.vendor(), connection.host(),
                connection.port(), connection.database(), Texts.mask(connection.username()), connection.status(),
                connection.lastMessage(), connection.lastCheckedAt(), connection.active(), connection.createdAt(),
                connection.updatedAt());
    }
}
