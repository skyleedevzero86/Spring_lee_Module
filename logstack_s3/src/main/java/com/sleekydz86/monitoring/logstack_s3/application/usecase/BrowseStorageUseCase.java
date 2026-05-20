package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.sleekydz86.monitoring.logstack_s3.application.assembler.StorageViewAssembler;
import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.application.query.BrowseStorageQuery;
import com.sleekydz86.monitoring.logstack_s3.application.view.StorageBrowseView;
import com.sleekydz86.monitoring.logstack_s3.application.view.StorageObjectView;
import com.sleekydz86.monitoring.logstack_s3.domain.model.ListedStorageObject;
import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseStorageUseCase implements UseCase<BrowseStorageQuery, StorageBrowseView> {

    private final ObjectStoragePort objectStorage;
    private final StorageViewAssembler assembler;

    @Override
    public StorageBrowseView apply(BrowseStorageQuery query) {
        log.info("S3 스토리지 조회: bucket={}, prefix={}, keyword={}",
                objectStorage.bucketName(), query.prefixFilter(), query.keyword());
        String keyPrefix = StorageObjectPaths.keyPrefix(query.prefixFilter());
        String keywordLower = query.keywordOptional()
                .map(k -> k.toLowerCase(Locale.ROOT))
                .orElse(null);

        List<StorageObjectView> views = objectStorage.listObjects().stream()
                .filter(object -> matchesPrefix(object, keyPrefix))
                .filter(object -> matchesKeyword(object, keywordLower))
                .sorted(Comparator.comparing(ListedStorageObject::lastModified).reversed())
                .map(assembler::toView)
                .toList();

        return new StorageBrowseView(objectStorage.bucketName(), views.size(), views);
    }

    private boolean matchesPrefix(ListedStorageObject object, String keyPrefix) {
        if (keyPrefix == null) {
            return true;
        }
        return object.key().startsWith(keyPrefix);
    }

    private boolean matchesKeyword(ListedStorageObject object, String keywordLower) {
        if (keywordLower == null) {
            return true;
        }
        return object.key().toLowerCase(Locale.ROOT).contains(keywordLower)
                || StorageObjectPaths.displayName(object.key()).toLowerCase(Locale.ROOT).contains(keywordLower);
    }
}
