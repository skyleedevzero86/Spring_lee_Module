package com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoredFileRow {

    private String id;
    private Long bucketId;
    private String originalFilename;
    private String objectKey;
    private String thumbnailKey;
    private String contentType;
    private long size;
    private LocalDateTime createdAt;
}
