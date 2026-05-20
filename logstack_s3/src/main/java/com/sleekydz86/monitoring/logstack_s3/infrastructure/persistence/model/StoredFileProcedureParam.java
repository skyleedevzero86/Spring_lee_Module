package com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoredFileProcedureParam {

    private String operation;
    private String id;
    private String originalFilename;
    private String objectKey;
    private String thumbnailKey;
    private String contentType;
    private long size;
    private Long bucketId;
    private LocalDateTime createdAt;
    private String dateTimePrefix;
}
