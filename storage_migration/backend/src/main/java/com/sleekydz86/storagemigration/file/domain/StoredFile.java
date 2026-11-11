package com.sleekydz86.storagemigration.file.domain;

import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.util.Require;
import com.sleekydz86.storagemigration.global.util.Texts;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class StoredFile {

    private final String id;
    private final String storageServerId;
    private final String bucket;
    private final String objectKey;
    private final String originalName;
    private String contentType;
    private final long size;
    private String checksum;
    private String title;
    private String description;
    private List<String> tags;
    private FileStatus status;
    private final Instant uploadedAt;
    private Instant savedAt;
    private Instant updatedAt;
    private String worker;
    private String workIp;

    private StoredFile(String id, String storageServerId, String bucket, String objectKey, String originalName,
                       String contentType, long size, String checksum, String title, String description,
                       List<String> tags, FileStatus status, Instant uploadedAt, Instant savedAt, Instant updatedAt,
                       String worker, String workIp) {
        this.id = id;
        this.storageServerId = storageServerId;
        this.bucket = bucket;
        this.objectKey = objectKey;
        this.originalName = originalName;
        this.contentType = contentType;
        this.size = size;
        this.checksum = checksum;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.status = status;
        this.uploadedAt = uploadedAt;
        this.savedAt = savedAt;
        this.updatedAt = updatedAt;
        this.worker = worker;
        this.workIp = workIp;
    }

    public static StoredFile uploaded(String id, String storageServerId, String bucket, String objectKey,
                                      String originalName, String contentType, long size, String checksum,
                                      Instant now) {
        if (size < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "파일 크기는 0 이상이어야 합니다.");
        }
        return new StoredFile(Require.notBlank(id, "id"), Require.notBlank(storageServerId, "스토리지 서버"),
                Require.notBlank(bucket, "버킷"), Require.notBlank(objectKey, "파일 키"),
                Require.notBlank(originalName, "파일 이름"), Texts.trimToNull(contentType), size, checksum,
                null, null, List.of(), FileStatus.UPLOADED, now, null, now, null, null);
    }

    public static StoredFile restore(String id, String storageServerId, String bucket, String objectKey,
                                     String originalName, String contentType, long size, String checksum, String title,
                                     String description, String tags, FileStatus status, Instant uploadedAt,
                                     Instant savedAt, Instant updatedAt, String worker, String workIp) {
        return new StoredFile(id, storageServerId, bucket, objectKey, originalName, contentType, size, checksum, title,
                description, parseTags(tags), status, uploadedAt, savedAt, updatedAt, worker, workIp);
    }

    public void updateDetail(String title, String description, List<String> tags, Instant now) {
        this.title = Require.maxLength(Texts.trimToNull(title), 200, "제목");
        this.description = Require.maxLength(Texts.trimToNull(description), 2000, "설명");
        this.tags = normalizeTags(tags);
        Require.maxLength(tagsText(), 500, "태그");
        this.updatedAt = now;
    }

    public void markSaved(String verifiedChecksum, Instant now) {
        if (Texts.hasText(verifiedChecksum)) {
            this.checksum = verifiedChecksum;
        }
        this.status = FileStatus.SAVED;
        this.savedAt = now;
        this.updatedAt = now;
    }

    public void correctContentType(String contentType) {
        if (Texts.hasText(contentType) && !Texts.hasText(this.contentType)) {
            this.contentType = contentType;
        }
    }

    public boolean isSaved() {
        return status == FileStatus.SAVED;
    }

    public String tagsText() {
        return tags.isEmpty() ? null : String.join(",", tags);
    }

    private static List<String> parseTags(String text) {
        if (!Texts.hasText(text)) {
            return List.of();
        }
        return normalizeTags(Arrays.asList(text.split(",")));
    }

    private static List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
                .filter(Texts::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    public String id() {
        return id;
    }

    public String storageServerId() {
        return storageServerId;
    }

    public String bucket() {
        return bucket;
    }

    public String objectKey() {
        return objectKey;
    }

    public String originalName() {
        return originalName;
    }

    public String contentType() {
        return contentType;
    }

    public long size() {
        return size;
    }

    public String checksum() {
        return checksum;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public List<String> tags() {
        return tags;
    }

    public FileStatus status() {
        return status;
    }

    public Instant uploadedAt() {
        return uploadedAt;
    }

    public Instant savedAt() {
        return savedAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public String worker() {
        return worker;
    }

    public String workIp() {
        return workIp;
    }
}
