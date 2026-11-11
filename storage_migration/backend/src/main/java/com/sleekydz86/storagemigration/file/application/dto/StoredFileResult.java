package com.sleekydz86.storagemigration.file.application.dto;

import com.sleekydz86.storagemigration.file.domain.FileStatus;
import com.sleekydz86.storagemigration.file.domain.StoredFile;
import java.time.Instant;
import java.util.List;

public record StoredFileResult(String id, String storageServerId, String bucket, String objectKey, String originalName,
                               String contentType, long size, String checksum, String title, String description,
                               List<String> tags, FileStatus status, Instant uploadedAt, Instant savedAt,
                               Instant updatedAt, String worker, String workIp) {

    public static StoredFileResult from(StoredFile file) {
        return new StoredFileResult(file.id(), file.storageServerId(), file.bucket(), file.objectKey(),
                file.originalName(), file.contentType(), file.size(), file.checksum(), file.title(), file.description(),
                file.tags(), file.status(), file.uploadedAt(), file.savedAt(), file.updatedAt(), file.worker(),
                file.workIp());
    }
}
