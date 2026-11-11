package com.sleekydz86.storagemigration.storage.domain;

import java.time.Instant;

public record StorageObject(String key, long size, Instant lastModified, String eTag, String contentType) {
}
