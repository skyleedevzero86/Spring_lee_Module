package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sleekydz86.monitoring.logstack_s3.application.assembler.FileViewAssembler;
import com.sleekydz86.monitoring.logstack_s3.application.query.SearchFilesQuery;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileListItemView;
import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchFilesUseCase implements UseCase<SearchFilesQuery, PageResult<FileListItemView>> {

    private final FileRepository fileRepository;
    private final FileViewAssembler assembler;

    @Override
    @Transactional(readOnly = true)
    public PageResult<FileListItemView> apply(SearchFilesQuery query) {
        log.debug("파일 목록 조회: page={}, size={}, keyword={}", query.page(), query.size(), query.keyword());
        return fileRepository
                .search(query.keywordOptional(), query.page(), query.size())
                .map(assembler::toListItem);
    }
}
