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

    public StorageObjectView toView(ListedStorageObject object) {
        String key = object.key();
        boolean image = StorageObjectPaths.isImageKey(key);
        String previewUrl = image ? objectStorage.presignPreview(key) : null;
        return new StorageObjectView(
                key,
                StorageObjectPaths.displayName(key),
                StorageObjectPaths.kindLabel(key),
                StorageSizeFormatter.format(object.sizeBytes()),
                object.lastModified(),
                previewUrl,
                image
        );
    }
}
