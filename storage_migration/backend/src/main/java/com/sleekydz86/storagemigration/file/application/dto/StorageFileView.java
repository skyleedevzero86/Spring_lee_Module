package com.sleekydz86.storagemigration.file.application.dto;

import com.sleekydz86.storagemigration.file.domain.FileStatus;
import java.time.Instant;

public record StorageFileView(String key, String name, long size, Instant lastModified, String storedFileId,
                              FileStatus status) {
}
