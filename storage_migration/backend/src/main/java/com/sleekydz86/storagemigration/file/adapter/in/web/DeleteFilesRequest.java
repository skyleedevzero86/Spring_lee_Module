package com.sleekydz86.storagemigration.file.adapter.in.web;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DeleteFilesRequest(@NotEmpty List<String> ids, boolean deleteFromStorage) {
}
