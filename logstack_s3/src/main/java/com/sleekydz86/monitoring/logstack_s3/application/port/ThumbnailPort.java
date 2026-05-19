package com.sleekydz86.monitoring.logstack_s3.application.port;

import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

public interface ThumbnailPort {

    Optional<byte[]> generate(MultipartFile file, String contentType);

    byte[] placeholder();

    String contentType();

    boolean supports(String contentType);
}
