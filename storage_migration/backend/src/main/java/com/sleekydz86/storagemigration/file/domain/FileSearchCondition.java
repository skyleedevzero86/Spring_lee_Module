package com.sleekydz86.storagemigration.file.domain;

public record FileSearchCondition(String keyword, String storageServerId, FileStatus status) {
}
