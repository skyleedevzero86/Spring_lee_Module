package com.sleekydz86.monitoring.logstack_s3.support;

import java.time.LocalDateTime;

import org.springframework.mock.web.MockMultipartFile;

import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFile;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFileSummary;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.StoredFileListRow;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.StoredFileRow;

public final class TestFileFixtures {

    public static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 5, 20, 14, 30);

    private TestFileFixtures() {
    }

    public static StoredFile storedFile() {
        return new StoredFile(
                "lky_20260520_1430_0001",
                "sample.png",
                "uploads/sample.png",
                "thumbnails/sample.jpg",
                "image/png",
                1024L,
                FIXED_TIME);
    }

    public static StoredFileSummary storedFileSummary() {
        return new StoredFileSummary(
                "lky_20260520_1430_0001",
                "sample.png",
                "uploads/sample.png",
                "thumbnails/sample.jpg",
                "image/png",
                1024L,
                FIXED_TIME,
                "erp-bucket",
                "us-east-1",
                "ERP LocalStack Bucket",
                "1 kB",
                "IMAGE");
    }

    public static StoredFileRow storedFileRow() {
        StoredFileRow row = new StoredFileRow();
        row.setId("lky_20260520_1430_0001");
        row.setBucketId(1L);
        row.setOriginalFilename("sample.png");
        row.setObjectKey("uploads/sample.png");
        row.setThumbnailKey("thumbnails/sample.jpg");
        row.setContentType("image/png");
        row.setSize(1024L);
        row.setCreatedAt(FIXED_TIME);
        return row;
    }

    public static StoredFileListRow storedFileListRow() {
        StoredFileListRow row = new StoredFileListRow();
        row.setId("lky_20260520_1430_0001");
        row.setBucketId(1L);
        row.setOriginalFilename("sample.png");
        row.setObjectKey("uploads/sample.png");
        row.setThumbnailKey("thumbnails/sample.jpg");
        row.setContentType("image/png");
        row.setSize(1024L);
        row.setCreatedAt(FIXED_TIME);
        row.setBucketCode("erp-bucket");
        row.setRegion("us-east-1");
        row.setBucketDisplayName("ERP LocalStack Bucket");
        row.setSizeLabel("1 kB");
        row.setMediaType("IMAGE");
        return row;
    }

    public static MockMultipartFile imageMultipartFile() {
        return new MockMultipartFile(
                "file",
                "sample.png",
                "image/png",
                new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 });
    }

    public static MockMultipartFile textMultipartFile() {
        return new MockMultipartFile(
                "file",
                "readme.txt",
                "text/plain",
                "hello".getBytes());
    }
}
