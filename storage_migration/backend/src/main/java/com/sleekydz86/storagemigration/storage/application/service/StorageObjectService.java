package com.sleekydz86.storagemigration.storage.application.service;

import com.sleekydz86.storagemigration.audit.application.port.in.RecordAuditUseCase;
import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.web.Actor;
import com.sleekydz86.storagemigration.storage.application.port.in.StorageObjectUseCase;
import com.sleekydz86.storagemigration.storage.application.port.out.ObjectStorageClient;
import com.sleekydz86.storagemigration.storage.application.port.out.StorageServerRepository;
import com.sleekydz86.storagemigration.storage.domain.StorageObject;
import com.sleekydz86.storagemigration.storage.domain.StorageObjectContent;
import com.sleekydz86.storagemigration.storage.domain.StorageServer;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class StorageObjectService implements StorageObjectUseCase {

    private final StorageServerRepository repository;
    private final ObjectStorageClient client;
    private final RecordAuditUseCase audit;

    public StorageObjectService(StorageServerRepository repository, ObjectStorageClient client,
                                RecordAuditUseCase audit) {
        this.repository = repository;
        this.client = client;
        this.audit = audit;
    }

    @Override
    public String bucketOf(String storageServerId) {
        return load(storageServerId).bucket();
    }

    @Override
    public List<StorageObject> list(String storageServerId, String prefix) {
        return client.list(load(storageServerId), prefix);
    }

    @Override
    public Optional<StorageObject> stat(String storageServerId, String key) {
        return client.stat(load(storageServerId), key);
    }

    @Override
    public void put(String storageServerId, String key, InputStream content, long size, String contentType) {
        client.put(load(storageServerId), key, content, size, contentType);
    }

    @Override
    public StorageObjectContent open(String storageServerId, String key) {
        return client.open(load(storageServerId), key);
    }

    @Override
    public int delete(String storageServerId, List<String> keys, Actor actor) {
        if (keys == null || keys.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "삭제할 파일을 선택하세요.");
        }
        StorageServer server = load(storageServerId);
        int deleted = 0;
        for (String key : keys) {
            client.delete(server, key);
            audit.record(actor, "STORAGE_OBJECT_DELETE", "STORAGE_OBJECT", server.id(), server.bucket() + "/" + key);
            deleted++;
        }
        return deleted;
    }

    private StorageServer load(String id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.STORAGE_SERVER_NOT_FOUND, id));
    }
}
