package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sleekydz86.monitoring.logstack_s3.application.assembler.FileViewAssembler;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileDetailView;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileNotFoundException;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetFileDetailUseCase implements UseCase<String, FileDetailView> {

    private final FileRepository fileRepository;
    private final FileViewAssembler assembler;

    @Override
    @Transactional(readOnly = true)
    public FileDetailView apply(String id) {
        log.info("파일 상세 조회 요청: id={}", id);
        return fileRepository.findById(id)
                .map(assembler::toDetail)
                .orElseThrow(() -> new FileNotFoundException(id));
    }
}
