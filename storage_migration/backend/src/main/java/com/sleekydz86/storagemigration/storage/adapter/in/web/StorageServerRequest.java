package com.sleekydz86.storagemigration.storage.adapter.in.web;

import com.sleekydz86.storagemigration.storage.domain.StorageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "스토리지 서버 등록·수정 요청")
public record StorageServerRequest(
        @Schema(description = "표시 이름", example = "로컬 MinIO") @NotBlank @Size(max = 100) String name,
        @Schema(description = "종류 (MINIO_CONSOLE 콘솔 9001, MINIO S3 API 9000, S3 AWS S3)", example = "MINIO_CONSOLE") @NotNull StorageType type,
        @Schema(description = "접속 주소. S3는 비우면 AWS 기본 주소", example = "http://localhost:9001") String endpoint,
        @Schema(description = "리전. 비우면 us-east-1", example = "us-east-1") String region,
        @Schema(description = "버킷 이름 (소문자·숫자·하이픈)", example = "files") @NotBlank String bucket,
        @Schema(description = "아이디. MinIO 콘솔은 로그인 아이디, S3 API·AWS S3는 Access Key. 수정할 때 비우면 기존 값 유지", example = "minioadmin") String accessKey,
        @Schema(description = "비밀번호. MinIO 콘솔은 로그인 비밀번호, S3 API·AWS S3는 Secret Key. 수정할 때 비우면 기존 값 유지", format = "password") String secretKey) {
}
