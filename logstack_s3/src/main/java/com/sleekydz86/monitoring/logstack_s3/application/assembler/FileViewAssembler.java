package com.sleekydz86.monitoring.logstack_s3.application.assembler;

import org.springframework.stereotype.Component;

import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileDetailView;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileListItemView;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFile;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFileSummary;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileViewAssembler {

    private final ObjectStoragePort objectStorage;

    public FileListItemView toListItem(StoredFileSummary summary) {
        return new FileListItemView(
                summary.id(),
                summary.originalFilename(),
                summary.contentType(),
                summary.size(),
                summary.createdAt(),
                summary.thumbnailKey() != null && !summary.thumbnailKey().isBlank()
                        ? objectStorage.presignPreview(summary.thumbnailKey())
                        : null,
                summary.bucketDisplayName(),
                summary.region(),
                summary.sizeLabel(),
                summary.mediaType()
        );
    }

    public FileDetailView toDetail(StoredFile file) {
        return new FileDetailView(
                file.id(),
                file.originalFilename(),
                file.contentType(),
                file.size(),
                file.createdAt(),
                file.objectKey(),
                file.thumbnailKeyOptional().map(objectStorage::presignPreview).orElse(null),
                objectStorage.presignPreview(file.objectKey()),
                objectStorage.presignDownload(file.objectKey(), file.originalFilename()),
                file.isImage(),
                file.isPdf()
        );
    }
}
