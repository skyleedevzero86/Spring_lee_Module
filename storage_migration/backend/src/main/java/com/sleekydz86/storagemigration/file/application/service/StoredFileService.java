package com.sleekydz86.storagemigration.file.application.service;

import com.sleekydz86.storagemigration.file.application.dto.SaveResult;
import com.sleekydz86.storagemigration.file.application.dto.StorageFileView;
import com.sleekydz86.storagemigration.file.application.dto.StoredFileResult;
import com.sleekydz86.storagemigration.file.application.dto.UpdateFileDetailCommand;
import com.sleekydz86.storagemigration.file.application.dto.UploadFileCommand;
import com.sleekydz86.storagemigration.file.application.port.in.DeleteStoredFilesUseCase;
import com.sleekydz86.storagemigration.file.application.port.in.FileQueryUseCase;
import com.sleekydz86.storagemigration.file.application.port.in.ImportStorageFilesUseCase;
import com.sleekydz86.storagemigration.file.application.port.in.SaveFileToDatabaseUseCase;
import com.sleekydz86.storagemigration.file.application.port.in.UpdateFileDetailUseCase;
import com.sleekydz86.storagemigration.file.application.port.in.UploadFileUseCase;
import com.sleekydz86.storagemigration.file.application.port.out.FileStoragePort;
import com.sleekydz86.storagemigration.file.application.port.out.StoredFileRepository;
import com.sleekydz86.storagemigration.file.domain.FileContent;
import com.sleekydz86.storagemigration.file.domain.FileSearchCondition;
import com.sleekydz86.storagemigration.file.domain.StorageEntry;
import com.sleekydz86.storagemigration.file.domain.StoredFile;
import com.sleekydz86.storagemigration.global.config.AppProperties;
import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.util.Checksums;
import com.sleekydz86.storagemigration.global.util.FileNames;
import com.sleekydz86.storagemigration.global.util.Ids;
import com.sleekydz86.storagemigration.global.web.Actor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class StoredFileService implements UploadFileUseCase, ImportStorageFilesUseCase, UpdateFileDetailUseCase,
        SaveFileToDatabaseUseCase, DeleteStoredFilesUseCase, FileQueryUseCase {

    private final StoredFileRepository repository;
    private final FileStoragePort storage;
    private final Clock clock;
    private final long maxDbContentBytes;

    public StoredFileService(StoredFileRepository repository, FileStoragePort storage, Clock clock,
                             AppProperties properties) {
        this.repository = repository;
        this.storage = storage;
        this.clock = clock;
        this.maxDbContentBytes = properties.maxDbContentBytes();
    }

    @Override
    public StoredFileResult upload(UploadFileCommand command, Actor actor) {
        if (command.size() <= 0 || command.content() == null) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        Instant now = clock.instant();
        String id = Ids.newId();
        String originalName = FileNames.sanitize(command.originalName());
        String key = FileNames.objectKey(id, originalName, now);
        MessageDigest digest = Checksums.sha256();
        try (InputStream input = new DigestInputStream(command.content(), digest)) {
            storage.put(command.storageServerId(), key, input, command.size(), command.contentType());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        StoredFile file = StoredFile.uploaded(id, command.storageServerId(), storage.bucketOf(command.storageServerId()),
                key, originalName, command.contentType(), command.size(), Checksums.toHex(digest), now);
        repository.create(file, actor);
        return StoredFileResult.from(file);
    }

    @Override
    public List<StoredFileResult> importFiles(String storageServerId, List<String> keys, Actor actor) {
        requireSelection(keys, "가져올 파일을 선택하세요.");
        Map<String, StoredFile> registered = repository.findByObjectKeys(storageServerId);
        String bucket = storage.bucketOf(storageServerId);
        List<StoredFileResult> results = new ArrayList<>();
        for (String key : keys) {
            StoredFile existing = registered.get(key);
            if (existing != null) {
                results.add(StoredFileResult.from(existing));
                continue;
            }
            StorageEntry entry = storage.stat(storageServerId, key)
                    .orElseThrow(() -> new BusinessException(ErrorCode.STORAGE_OBJECT_NOT_FOUND, key));
            StoredFile file = StoredFile.uploaded(Ids.newId(), storageServerId, bucket, key, FileNames.nameOf(key),
                    entry.contentType(), entry.size(), null, clock.instant());
            repository.create(file, actor);
            results.add(StoredFileResult.from(file));
        }
        return results;
    }

    @Override
    public StoredFileResult update(UpdateFileDetailCommand command, Actor actor) {
        StoredFile file = load(command.id());
        file.updateDetail(command.title(), command.description(), command.tags(), clock.instant());
        repository.update(file, actor);
        return StoredFileResult.from(file);
    }

    @Override
    public SaveResult save(UpdateFileDetailCommand detail, Actor actor) {
        StoredFile file = load(detail.id());
        Instant now = clock.instant();
        file.updateDetail(detail.title(), detail.description(), detail.tags(), now);
        FileContent content = storage.open(file.storageServerId(), file.objectKey());
        long size = content.size() > 0 ? content.size() : file.size();
        if (size > maxDbContentBytes) {
            closeQuietly(content.stream());
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE, size + " bytes > " + maxDbContentBytes + " bytes");
        }
        MessageDigest digest = Checksums.sha256();
        try (InputStream input = new DigestInputStream(content.stream(), digest)) {
            repository.saveContent(file.id(), input, size, now, actor);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        String streamedChecksum = Checksums.toHex(digest);
        long databaseBytes = repository.contentLength(file.id());
        boolean checksumMatches = file.checksum() == null || file.checksum().equals(streamedChecksum);
        boolean verified = databaseBytes == size && checksumMatches;
        file.correctContentType(content.contentType());
        file.markSaved(streamedChecksum, now);
        repository.update(file, actor);
        return new SaveResult(StoredFileResult.from(file), size, databaseBytes, streamedChecksum, verified);
    }

    @Override
    public int delete(List<String> ids, boolean deleteFromStorage, Actor actor) {
        requireSelection(ids, "삭제할 파일을 선택하세요.");
        int deleted = 0;
        for (String id : ids) {
            StoredFile file = load(id);
            if (deleteFromStorage) {
                storage.delete(file.storageServerId(), file.objectKey(), actor);
            }
            repository.delete(file, actor);
            deleted++;
        }
        return deleted;
    }

    @Override
    public int deleteStorageObjects(String storageServerId, List<String> keys, Actor actor) {
        requireSelection(keys, "삭제할 파일을 선택하세요.");
        Map<String, StoredFile> registered = repository.findByObjectKeys(storageServerId);
        int deleted = 0;
        for (String key : keys) {
            storage.delete(storageServerId, key, actor);
            StoredFile file = registered.get(key);
            if (file != null) {
                repository.delete(file, actor);
            }
            deleted++;
        }
        return deleted;
    }

    @Override
    public StoredFileResult get(String id) {
        return StoredFileResult.from(load(id));
    }

    @Override
    public List<StoredFileResult> search(FileSearchCondition condition) {
        return repository.search(condition).stream().map(StoredFileResult::from).toList();
    }

    @Override
    public List<StorageFileView> browseStorage(String storageServerId, String prefix) {
        Map<String, StoredFile> registered = repository.findByObjectKeys(storageServerId);
        return storage.list(storageServerId, prefix).stream()
                .map(entry -> {
                    StoredFile file = registered.get(entry.key());
                    return new StorageFileView(entry.key(), FileNames.nameOf(entry.key()), entry.size(),
                            entry.lastModified(), file == null ? null : file.id(), file == null ? null : file.status());
                })
                .toList();
    }

    @Override
    public FileContent openContent(String id) {
        StoredFile file = load(id);
        try {
            FileContent content = storage.open(file.storageServerId(), file.objectKey());
            String type = content.contentType() == null ? file.contentType() : content.contentType();
            return new FileContent(content.stream(), content.size(), type, file.originalName());
        } catch (BusinessException exception) {
            if (!file.isSaved()) {
                throw exception;
            }
            InputStream stream = repository.openContent(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND, id));
            return new FileContent(stream, file.size(), file.contentType(), file.originalName());
        }
    }

    @Override
    public FileContent openDatabaseContent(String id) {
        StoredFile file = load(id);
        if (!file.isSaved()) {
            throw new BusinessException(ErrorCode.FILE_NOT_SAVED_IN_DB, file.originalName());
        }
        InputStream stream = repository.openContent(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_SAVED_IN_DB, file.originalName()));
        return new FileContent(stream, repository.contentLength(id), file.contentType(), file.originalName());
    }

    private StoredFile load(String id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND, id));
    }

    private static void requireSelection(List<String> values, String message) {
        if (values == null || values.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, message);
        }
    }

    private static void closeQuietly(InputStream stream) {
        try {
            stream.close();
        } catch (IOException ignored) {
        }
    }
}
