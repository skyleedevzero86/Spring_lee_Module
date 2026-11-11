package com.sleekydz86.storagemigration.file.domain;

import java.io.InputStream;

public record FileContent(InputStream stream, long size, String contentType, String fileName) {
}
