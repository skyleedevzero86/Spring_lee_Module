package com.sleekydz86.storagemigration.file.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record StorageKeysRequest(@NotBlank String storageServerId, @NotEmpty List<String> keys) {
}
