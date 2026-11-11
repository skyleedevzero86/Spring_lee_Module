package com.sleekydz86.storagemigration.file.application.dto;

import java.util.List;

public record UpdateFileDetailCommand(String id, String title, String description, List<String> tags) {
}
