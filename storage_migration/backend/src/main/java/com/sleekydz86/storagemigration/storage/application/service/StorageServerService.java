package com.sleekydz86.storagemigration.storage.application.service;

import com.sleekydz86.storagemigration.audit.application.port.in.RecordAuditUseCase;
import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.util.Ids;
import com.sleekydz86.storagemigration.global.web.Actor;
import com.sleekydz86.storagemigration.storage.application.dto.RegisterStorageServerCommand;
import com.sleekydz86.storagemigration.storage.application.dto.StorageServerResult;
import com.sleekydz86.storagemigration.storage.application.dto.UpdateStorageServerCommand;
import com.sleekydz86.storagemigration.storage.application.port.in.RegisterStorageServerUseCase;
import com.sleekydz86.storagemigration.storage.application.port.in.StorageServerQueryUseCase;
import com.sleekydz86.storagemigration.storage.application.port.in.TestStorageServerUseCase;
import com.sleekydz86.storagemigration.storage.application.port.in.UpdateStorageServerUseCase;
import com.sleekydz86.storagemigration.storage.application.port.out.ObjectStorageClient;
import com.sleekydz86.storagemigration.storage.application.port.out.StorageServerRepository;
import com.sleekydz86.storagemigration.storage.domain.StorageServer;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StorageServerService implements RegisterStorageServerUseCase, UpdateStorageServerUseCase,
        TestStorageServerUseCase, StorageServerQueryUseCase {

    private final StorageServerRepository repository;
    private final ObjectStorageClient client;
    private final RecordAuditUseCase audit;
    private final Clock clock;

    public StorageServerService(StorageServerRepository repository, ObjectStorageClient client,
                                RecordAuditUseCase audit, Clock clock) {
        this.repository = repository;
        this.client = client;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    public StorageServerResult register(RegisterStorageServerCommand command, Actor actor) {
        StorageServer server = StorageServer.register(Ids.newId(), command.name(), command.type(), command.endpoint(),
                command.region(), command.bucket(), command.accessKey(), command.secretKey(), clock.instant());
        repository.create(server, actor);
        return StorageServerResult.from(server);
    }

    @Override
    public StorageServerResult update(UpdateStorageServerCommand command, Actor actor) {
        StorageServer server = load(command.id());
        server.changeSettings(command.name(), command.type(), command.endpoint(), command.region(), command.bucket(),
                command.accessKey(), command.secretKey(), clock.instant());
        repository.update(server, actor);
        return StorageServerResult.from(server);
    }

    @Override
    public StorageServerResult test(String id, boolean createBucketIfMissing, Actor actor) {
        StorageServer server = load(id);
        try {
            String message = client.checkConnection(server, createBucketIfMissing);
            server.recordSuccess(message, clock.instant());
        } catch (BusinessException exception) {
            server.recordFailure(exception.getMessage(), clock.instant());
        }
        repository.update(server, actor);
        audit.record(actor, "STORAGE_SERVER_TEST", "STORAGE_SERVER", server.id(),
                server.status() + " " + server.lastMessage());
        return StorageServerResult.from(server);
    }

    @Override
    public StorageServerResult get(String id) {
        return StorageServerResult.from(load(id));
    }

    @Override
    public List<StorageServerResult> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(StorageServer::createdAt))
                .map(StorageServerResult::from)
                .toList();
    }

    private StorageServer load(String id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.STORAGE_SERVER_NOT_FOUND, id));
    }
}
