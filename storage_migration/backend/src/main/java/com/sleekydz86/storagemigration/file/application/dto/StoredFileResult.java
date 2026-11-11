package com.sleekydz86.storagemigration.file.application.dto;

import com.sleekydz86.storagemigration.file.domain.FileStatus;
import com.sleekydz86.storagemigration.file.domain.StoredFile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "등록된 파일 정보 (SM_STORED_FILE)")
public record StoredFileResult(
        @Schema(description = "파일 ID (UUID)") String id,
        @Schema(description = "스토리지 서버 ID") String storageServerId,
        @Schema(description = "버킷 이름") String bucket,
        @Schema(description = "스토리지 객체 키") String objectKey,
        @Schema(description = "원본 파일 이름") String originalName,
        @Schema(description = "파일 형식 (Content-Type)") String contentType,
        @Schema(description = "파일 크기 (바이트)") long size,
        @Schema(description = "파일 내용의 SHA-256 체크섬 (16진수 64자)") String checksum,
        @Schema(description = "제목") String title,
        @Schema(description = "설명") String description,
        @Schema(description = "태그 목록") List<String> tags,
        @Schema(description = "상태 (UPLOADED 스토리지에만 있음, SAVED DB에도 저장됨)") FileStatus status,
        @Schema(description = "스토리지에 올린 일시") Instant uploadedAt,
        @Schema(description = "DB에 내용을 저장한 일시") Instant savedAt,
        @Schema(description = "마지막 수정 일시") Instant updatedAt,
        @Schema(description = "마지막 작업자") String worker,
        @Schema(description = "마지막 작업 IP") String workIp) {

    public static StoredFileResult from(StoredFile file) {
        return new StoredFileResult(file.id(), file.storageServerId(), file.bucket(), file.objectKey(),
                file.originalName(), file.contentType(), file.size(), file.checksum(), file.title(), file.description(),
                file.tags(), file.status(), file.uploadedAt(), file.savedAt(), file.updatedAt(), file.worker(),
                file.workIp());
    }
}
