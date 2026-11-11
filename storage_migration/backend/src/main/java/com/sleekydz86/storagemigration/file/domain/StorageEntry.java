package com.sleekydz86.storagemigration.file.domain;

import java.time.Instant;

public record StorageEntry(String key, long size, Instant lastModified, String contentType) {
}
