package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import org.springframework.stereotype.Service;

import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.application.query.GetStoragePreviewQuery;
import com.sleekydz86.monitoring.logstack_s3.application.view.StoragePreviewView;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileNotFoundException;
import com.sleekydz86.monitoring.logstack_s3.domain.message.DomainMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.StorageBucketRepository;
import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetStoragePreviewUseCase implements UseCase<GetStoragePreviewQuery, StoragePreviewView> {

    private final StorageBucketRepository storageBucketRepository;
    private final ObjectStoragePort objectStorage;

    @Override
    public StoragePreviewView apply(GetStoragePreviewQuery query) {
        var bucket = storageBucketRepository.findByBucketCode(query.bucketCode())
                .orElseThrow(() -> new FileNotFoundException(DomainMessages.bucketNotFound(query.bucketCode())));

        String bucketCode = bucket.bucketCode();
        String objectKey = query.objectKey();
        boolean found = objectStorage.listObjects(bucketCode).stream()
                .anyMatch(object -> object.key().equals(objectKey));
        if (!found) {
            throw new FileNotFoundException(DomainMessages.storageObjectNotFound(objectKey));
        }

        String originalKey = resolveOriginalKey(bucketCode, objectKey);
        boolean image = StorageObjectPaths.isImageKey(originalKey);
        String previewUrl = image ? objectStorage.presignPreview(bucketCode, originalKey) : null;

        return new StoragePreviewView(
                bucketCode,
                bucket.displayName(),
                objectKey,
                originalKey,
                StorageObjectPaths.displayName(originalKey),
                previewUrl,
                image);
    }

    private String resolveOriginalKey(String bucketCode, String objectKey) {
        if (!StorageObjectPaths.isThumbnailKey(objectKey)) {
            return objectKey;
        }
        String uploadsPrefix = StorageObjectPaths.uploadsPrefixForThumbnail(objectKey);
        if (uploadsPrefix == null) {
            return objectKey;
        }
        return objectStorage.findFirstObjectKey(bucketCode, uploadsPrefix).orElse(objectKey);
    }
}
