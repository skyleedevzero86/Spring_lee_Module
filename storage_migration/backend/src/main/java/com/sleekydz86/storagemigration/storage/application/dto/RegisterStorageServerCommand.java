package com.sleekydz86.storagemigration.storage.application.dto;

import com.sleekydz86.storagemigration.storage.domain.StorageType;

public record RegisterStorageServerCommand(String name, StorageType type, String endpoint, String region, String bucket,
                                           String accessKey, String secretKey) {
}
