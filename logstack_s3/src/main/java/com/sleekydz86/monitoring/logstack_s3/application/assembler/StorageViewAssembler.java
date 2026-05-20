package com.sleekydz86.monitoring.logstack_s3.application.assembler;

import org.springframework.stereotype.Component;

import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.application.view.StorageObjectView;
import com.sleekydz86.monitoring.logstack_s3.domain.model.ListedStorageObject;
import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;
import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageSizeFormatter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StorageViewAssembler {

    private final ObjectStoragePort objectStorage;

    public StorageObjectView toView(String bucketCode, ListedStorageObject object) {
        String key = object.key();
        boolean image = StorageObjectPaths.isImageKey(key);
        boolean thumbnail = StorageObjectPaths.isThumbnailKey(key);
        String previewUrl = image ? objectStorage.presignPreview(bucketCode, key) : null;
        String originalPreviewUrl = resolveOriginalPreviewUrl(bucketCode, key, image, thumbnail, previewUrl);
        return new StorageObjectView(
                key,
                StorageObjectPaths.displayName(key),
                StorageObjectPaths.kindLabel(key),
                StorageSizeFormatter.format(object.sizeBytes()),
                object.lastModified(),
                previewUrl,
                originalPreviewUrl,
                image,
                thumbnail);
    }

    private String resolveOriginalPreviewUrl(
            String bucketCode,
            String key,
            boolean image,
            boolean thumbnail,
            String previewUrl
    ) {
        if (!image) {
            return null;
        }
        if (!thumbnail) {
            return previewUrl;
        }
        String uploadsPrefix = StorageObjectPaths.uploadsPrefixForThumbnail(key);
        if (uploadsPrefix == null) {
            return previewUrl;
        }
        return objectStorage.findFirstObjectKey(bucketCode, uploadsPrefix)
                .map(originalKey -> objectStorage.presignPreview(bucketCode, originalKey))
                .orElse(previewUrl);
    }
}
