package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sleekydz86.monitoring.logstack_s3.application.assembler.StorageBucketViewAssembler;
import com.sleekydz86.monitoring.logstack_s3.application.query.ListStorageBucketsQuery;
import com.sleekydz86.monitoring.logstack_s3.application.view.StorageBucketView;
import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.StorageBucketRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListStorageBucketsUseCase implements UseCase<ListStorageBucketsQuery, PageResult<StorageBucketView>> {

    private final StorageBucketRepository storageBucketRepository;
    private final StorageBucketViewAssembler assembler;

    @Override
    @Transactional(readOnly = true)
    public PageResult<StorageBucketView> apply(ListStorageBucketsQuery query) {
        log.info("스토리지 버킷 목록: page={}, size={}, keyword={}", query.page(), query.size(), query.keyword());
        return storageBucketRepository
                .search(query.keywordOptional(), query.page(), query.size())
                .map(assembler::toView);
    }
}
