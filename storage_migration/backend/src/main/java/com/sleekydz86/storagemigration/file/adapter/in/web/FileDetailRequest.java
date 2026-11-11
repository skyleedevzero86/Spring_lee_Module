package com.sleekydz86.storagemigration.file.adapter.in.web;

import jakarta.validation.constraints.Size;
import java.util.List;

public record FileDetailRequest(@Size(max = 200) String title, @Size(max = 2000) String description, List<String> tags) {
}
