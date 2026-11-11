package com.sleekydz86.storagemigration.storage.domain;

import java.io.InputStream;

public record StorageObjectContent(InputStream stream, long size, String contentType) {
}
