package com.sleekydz86.storagemigration.connection.adapter.in.web;

import com.sleekydz86.storagemigration.connection.domain.DbVendor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DbConnectionRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull DbVendor vendor,
        @NotBlank String host,
        @Min(1) @Max(65535) Integer port,
        @NotBlank String database,
        String username,
        String password) {
}
