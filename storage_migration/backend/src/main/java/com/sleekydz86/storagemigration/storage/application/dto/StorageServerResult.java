package com.sleekydz86.storagemigration.storage.application.dto;

import com.sleekydz86.storagemigration.global.util.Texts;
import com.sleekydz86.storagemigration.storage.domain.StorageServer;
import com.sleekydz86.storagemigration.storage.domain.StorageStatus;
import com.sleekydz86.storagemigration.storage.domain.StorageType;
import java.time.Instant;

public record StorageServerResult(String id, String name, StorageType type, String endpoint, String region,
                                  String bucket, String maskedAccessKey, StorageStatus status, String lastMessage,
                                  Instant lastCheckedAt, Instant createdAt, Instant updatedAt) {

    public static StorageServerResult from(StorageServer server) {
        return new StorageServerResult(server.id(), server.name(), server.type(), server.endpoint(), server.region(),
                server.bucket(), Texts.mask(server.accessKey()), server.status(), server.lastMessage(),
                server.lastCheckedAt(), server.createdAt(), server.updatedAt());
    }
}
