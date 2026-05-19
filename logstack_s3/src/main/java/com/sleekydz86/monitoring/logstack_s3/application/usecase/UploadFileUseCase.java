package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sleekydz86.monitoring.logstack_s3.application.assembler.FileViewAssembler;
import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.application.port.ThumbnailPort;
import com.sleekydz86.monitoring.logstack_s3.application.query.UploadFileCommand;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileDetailView;
import com.sleekydz86.monitoring.logstack_s3.global.common.message.KoreanMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileStorageException;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFile;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;
import com.sleekydz86.monitoring.logstack_s3.domain.service.FileKeyFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadFileUseCase implements UseCase<UploadFileCommand, FileDetailView> {

    private final FileRepository fileRepository;
    private final ObjectStoragePort objectStorage;
    private final ThumbnailPort thumbnailPort;
    private final FileViewAssembler assembler;

    @Override
    @Transactional
    public FileDetailView apply(UploadFileCommand command) {
        MultipartFile file = command.file();
        String filename = FileKeyFactory.defaultFilename(file.getOriginalFilename());
        log.info("파일 업로드 시작: 파일명={}, 크기={}bytes", filename, file.getSize());

        String contentType = FileKeyFactory.defaultContentType(file.getContentType());
        String objectKey = FileKeyFactory.uploadKey(filename);

        uploadOriginal(file, objectKey, contentType);
        String thumbnailKey = resolveThumbnailKey(file, contentType);

        StoredFile saved = fileRepository.save(StoredFile.draft(
                filename, objectKey, thumbnailKey, contentType, file.getSize()
        ));
        log.info("파일 업로드 완료: id={}, 파일명={}", saved.id(), filename);
        return assembler.toDetail(saved);
    }

    private void uploadOriginal(MultipartFile file, String objectKey, String contentType) {
        try {
            objectStorage.put(objectKey, contentType, file.getSize(), file.getInputStream());
        } catch (IOException e) {
            throw new FileStorageException(KoreanMessages.FILE_READ_FAILED, e);
        }
    }

    private String resolveThumbnailKey(MultipartFile file, String contentType) {
        byte[] bytes = thumbnailPort.generate(file, contentType)
                .or(() -> Optional.ofNullable(thumbnailPort.supports(contentType) ? null : thumbnailPort.placeholder()))
                .orElseGet(thumbnailPort::placeholder);
        return storeThumbnail(bytes);
    }

    private String storeThumbnail(byte[] bytes) {
        String key = FileKeyFactory.thumbnailKey();
        objectStorage.putBytes(key, thumbnailPort.contentType(), bytes);
        return key;
    }
}
