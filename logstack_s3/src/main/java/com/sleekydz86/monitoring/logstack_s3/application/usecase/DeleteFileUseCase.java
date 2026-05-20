package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileNotFoundException;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFile;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteFileUseCase implements UseCase<String, Void> {

    private final FileRepository fileRepository;
    private final ObjectStoragePort objectStorage;

    @Override
    @Transactional
    public Void apply(String id) {
        log.info("파일 삭제 요청: id={}", id);
        StoredFile file = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(id));

        deleteFromStorage(file.objectKey());
        file.thumbnailKeyOptional().ifPresent(this::deleteFromStorage);

        fileRepository.delete(id);
        log.info("파일 삭제 완료: id={}", id);
        return null;
    }

    private void deleteFromStorage(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        objectStorage.delete(key);
        log.debug("S3 객체 삭제: key={}", key);
    }
}
