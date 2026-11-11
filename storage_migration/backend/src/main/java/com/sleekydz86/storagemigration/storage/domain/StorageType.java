package com.sleekydz86.storagemigration.storage.domain;

public enum StorageType {
    MINIO, MINIO_CONSOLE, S3;

    public boolean requiresEndpoint() {
        return this == MINIO || this == MINIO_CONSOLE;
    }
}
