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
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileNotFoundException;
import com.sleekydz86.monitoring.logstack_s3.domain.message.DomainMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.model.ListedStorageObject;
import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.StorageBucketRepository;
import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseStorageUseCase implements UseCase<BrowseStorageQuery, StorageBrowseView> {

    private final StorageBucketRepository storageBucketRepository;
    private final ObjectStoragePort objectStorage;
    private final StorageViewAssembler assembler;

    @Override
    public StorageBrowseView apply(BrowseStorageQuery query) {
        var bucket = storageBucketRepository.findByBucketCode(query.bucketCode())
                .orElseThrow(() -> new FileNotFoundException(DomainMessages.bucketNotFound(query.bucketCode())));

        log.info("S3 객체 조회: bucket={}, prefix={}, keyword={}, page={}, size={}",
                query.bucketCode(), query.prefixFilter(), query.keyword(), query.page(), query.size());

        String keyPrefix = StorageObjectPaths.keyPrefix(query.prefixFilter());
        String keywordLower = query.keywordOptional()
                .map(k -> k.toLowerCase(Locale.ROOT))
                .orElse(null);

        List<StorageObjectView> filtered = objectStorage.listObjects(bucket.bucketCode()).stream()
                .filter(object -> matchesPrefix(object, keyPrefix))
                .filter(object -> matchesKeyword(object, keywordLower))
                .sorted(Comparator.comparing(ListedStorageObject::lastModified).reversed())
                .map(object -> assembler.toView(bucket.bucketCode(), object))
                .toList();

        int from = query.page() * query.size();
        int to = Math.min(from + query.size(), filtered.size());
        List<StorageObjectView> pageContent = from >= filtered.size()
                ? List.of()
                : filtered.subList(from, to);

        PageResult<StorageObjectView> page = PageResult.of(
                pageContent,
                query.page(),
                query.size(),
                filtered.size());

        return new StorageBrowseView(
                bucket.bucketCode(),
                bucket.displayName(),
                bucket.region(),
                page);
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
