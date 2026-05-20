package com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageBucketRow {

    private long id;
    private String bucketCode;
    private String region;
    private String displayName;
    private LocalDateTime createdAt;
}
