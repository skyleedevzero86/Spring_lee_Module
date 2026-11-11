package com.sleekydz86.storagemigration.connection.adapter.in.web;

import com.sleekydz86.storagemigration.connection.domain.DbVendor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DB 연결 등록·수정 요청")
public record DbConnectionRequest(
        @Schema(description = "표시 이름", example = "개발 PostgreSQL") @NotBlank @Size(max = 100) String name,
        @Schema(description = "DB 종류", example = "POSTGRESQL") @NotNull DbVendor vendor,
        @Schema(description = "호스트", example = "localhost") @NotBlank String host,
        @Schema(description = "포트. 비우면 기본 포트 (Oracle 1521, MySQL 3306, PostgreSQL 5432)", example = "5432") @Min(1) @Max(65535) Integer port,
        @Schema(description = "DB 이름. Oracle은 서비스 이름", example = "storage_migration") @NotBlank String database,
        @Schema(description = "아이디. 수정할 때 비우면 기존 값을 유지합니다", example = "app") String username,
        @Schema(description = "비밀번호. 수정할 때 비우면 기존 값을 유지합니다", format = "password") String password) {
}
