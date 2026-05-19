package com.sleekydz86.monitoring.logstack_s3.application.query;

import org.springframework.web.multipart.MultipartFile;

public record UploadFileCommand(MultipartFile file) {
}
