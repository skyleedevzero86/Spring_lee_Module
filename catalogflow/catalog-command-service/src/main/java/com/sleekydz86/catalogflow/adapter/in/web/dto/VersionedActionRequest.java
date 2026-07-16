package com.sleekydz86.catalogflow.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VersionedActionRequest(
		@NotNull Long version,
		@Size(max = 500) String reason) {
}
