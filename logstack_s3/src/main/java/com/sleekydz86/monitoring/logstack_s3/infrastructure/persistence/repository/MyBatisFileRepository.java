package com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sleekydz86.monitoring.logstack_s3.domain.exception.InvalidRequestException;
import com.sleekydz86.monitoring.logstack_s3.domain.message.DomainMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFile;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFileSummary;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;
import com.sleekydz86.monitoring.logstack_s3.domain.service.FileIdGenerator;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.mapper.StoredFileMapper;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.FileOperation;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.StoredFileListRow;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.StoredFileProcedureParam;
import com.sleekydz86.monitoring.logstack_s3.infrastructure.persistence.model.StoredFileRow;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MyBatisFileRepository implements FileRepository {

    private final StoredFileMapper mapper;

    @Value("${logstack.default-bucket-id:1}")
    private long defaultBucketId;

    @Value("${logstack.file-id.prefix:lky}")
    private String fileIdPrefix;

    @Override
    @Transactional
    public StoredFile save(StoredFile file) {
        String dateTimePrefix = FileIdGenerator.dateTimePrefix(fileIdPrefix, file.createdAt());
        long sequence = allocateNextSequence(dateTimePrefix);
        String newId = FileIdGenerator.formatId(dateTimePrefix, sequence);
        StoredFile toSave = file.withId(newId);
        callProcedure(FileOperation.C, toSave);
        return toSave;
    }

    @Override
    @Transactional
    public StoredFile update(StoredFile file) {
        if (file.id() == null || file.id().isBlank()) {
            throw new InvalidRequestException(DomainMessages.ID_REQUIRED_FOR_UPDATE);
        }
        callProcedure(FileOperation.U, file);
        return file;
    }

    @Override
    @Transactional
    public void delete(String id) {
        StoredFileProcedureParam param = new StoredFileProcedureParam();
        param.setOperation(FileOperation.D.code());
        param.setId(id);
        param.setOriginalFilename("");
        param.setObjectKey("");
        param.setContentType("application/octet-stream");
        param.setSize(0L);
        param.setBucketId(defaultBucketId);
        param.setCreatedAt(java.time.LocalDateTime.of(1970, 1, 1, 0, 0));
        mapper.callManage(param);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StoredFile> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<StoredFileSummary> search(Optional<String> keyword, int page, int size) {
        String kw = keyword.orElse(null);
        int offset = page * size;
        long total = mapper.countFromView(kw);
        List<StoredFileSummary> content = mapper.selectPageFromView(kw, offset, size).stream()
                .map(this::toSummary)
                .toList();
        return PageResult.of(content, page, size, total);
    }

    @Override
    @Transactional
    public void seedDemoData(int count) {
        var uploadedAt = java.time.LocalDateTime.now();
        String dateTimePrefix = FileIdGenerator.dateTimePrefix(fileIdPrefix, uploadedAt);
        for (int i = 1; i <= count; i++) {
            long sequence = allocateNextSequence(dateTimePrefix);
            String id = FileIdGenerator.formatId(dateTimePrefix, sequence);
            StoredFile seed = new StoredFile(
                    id,
                    "demo_" + i + ".dat",
                    "uploads/seed/" + UUID.randomUUID() + ".dat",
                    "thumbnails/seed/" + i + ".jpg",
                    i % 3 == 0 ? "image/jpeg" : (i % 3 == 1 ? "application/pdf" : "application/octet-stream"),
                    1000L + i,
                    uploadedAt);
            callProcedure(FileOperation.C, seed);
        }
    }

    private long allocateNextSequence(String dateTimePrefix) {
        StoredFileProcedureParam param = new StoredFileProcedureParam();
        param.setOperation(FileOperation.S.code());
        param.setDateTimePrefix(dateTimePrefix);
        Long sequence = mapper.callManage(param);
        if (sequence == null) {
            throw new IllegalStateException("function S did not return last_sequence for prefix=" + dateTimePrefix);
        }
        return sequence;
    }

    private void callProcedure(FileOperation operation, StoredFile file) {
        StoredFileProcedureParam param = new StoredFileProcedureParam();
        param.setOperation(operation.code());
        param.setId(file.id());
        param.setOriginalFilename(file.originalFilename());
        param.setObjectKey(file.objectKey());
        param.setThumbnailKey(file.thumbnailKey());
        param.setContentType(file.contentType());
        param.setSize(file.size());
        param.setBucketId(defaultBucketId);
        param.setCreatedAt(file.createdAt());
        mapper.callManage(param);
    }

    private StoredFile toDomain(StoredFileRow row) {
        return new StoredFile(
                row.getId(),
                row.getOriginalFilename(),
                row.getObjectKey(),
                row.getThumbnailKey(),
                row.getContentType(),
                row.getSize(),
                row.getCreatedAt());
    }

    private StoredFileSummary toSummary(StoredFileListRow row) {
        return new StoredFileSummary(
                row.getId(),
                row.getOriginalFilename(),
                row.getObjectKey(),
                row.getThumbnailKey(),
                row.getContentType(),
                row.getSize(),
                row.getCreatedAt(),
                row.getBucketCode(),
                row.getRegion(),
                row.getBucketDisplayName(),
                row.getSizeLabel(),
                row.getMediaType());
    }
}
