package com.sleekydz86.storagemigration.file.application.dto;

import java.io.InputStream;

public record UploadFileCommand(String storageServerId, String originalName, String contentType, long size,
                                InputStream content) {
}
