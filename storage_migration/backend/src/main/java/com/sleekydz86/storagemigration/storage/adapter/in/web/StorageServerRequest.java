package com.sleekydz86.storagemigration.storage.adapter.in.web;

import com.sleekydz86.storagemigration.storage.domain.StorageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StorageServerRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull StorageType type,
        String endpoint,
        String region,
        @NotBlank String bucket,
        String accessKey,
        String secretKey) {
}
