package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sleekydz86.monitoring.logstack_s3.domain.message.DomainMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.InvalidRequestException;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeedFilesUseCase implements UseCase<Integer, Integer> {

    private final FileRepository fileRepository;

    @Override
    @Transactional
    public Integer apply(Integer count) {
        if (count == null || count < 1) {
            throw new InvalidRequestException(DomainMessages.SEED_COUNT_MIN);
        }
        if (count > 500_000) {
            throw new InvalidRequestException(DomainMessages.SEED_COUNT_MAX);
        }
        log.info("데모 데이터 시드 시작: {}건", count);
        fileRepository.seedDemoData(count);
        log.info("데모 데이터 시드 완료: {}건", count);
        return count;
    }
}
