package com.sleekydz86.storagemigration.connection.application.service;

import com.sleekydz86.storagemigration.connection.application.dto.DbConnectionResult;
import com.sleekydz86.storagemigration.connection.application.dto.RegisterDbConnectionCommand;
import com.sleekydz86.storagemigration.connection.application.dto.UpdateDbConnectionCommand;
import com.sleekydz86.storagemigration.connection.application.port.in.ActivateDbConnectionUseCase;
import com.sleekydz86.storagemigration.connection.application.port.in.DbConnectionQueryUseCase;
import com.sleekydz86.storagemigration.connection.application.port.in.RegisterDbConnectionUseCase;
import com.sleekydz86.storagemigration.connection.application.port.in.TestDbConnectionUseCase;
import com.sleekydz86.storagemigration.connection.application.port.in.UpdateDbConnectionUseCase;
import com.sleekydz86.storagemigration.connection.application.port.out.DatabaseActivationPort;
import com.sleekydz86.storagemigration.connection.application.port.out.DbConnectionProbe;
import com.sleekydz86.storagemigration.connection.application.port.out.DbConnectionProbe.ProbeResult;
import com.sleekydz86.storagemigration.connection.application.port.out.DbConnectionRepository;
import com.sleekydz86.storagemigration.connection.domain.DbConnection;
import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.util.Ids;
import com.sleekydz86.storagemigration.global.web.Actor;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DbConnectionService implements RegisterDbConnectionUseCase, UpdateDbConnectionUseCase,
        TestDbConnectionUseCase, ActivateDbConnectionUseCase, DbConnectionQueryUseCase {

    private final DbConnectionRepository repository;
    private final DbConnectionProbe probe;
    private final DatabaseActivationPort activationPort;
    private final Clock clock;

    public DbConnectionService(DbConnectionRepository repository, DbConnectionProbe probe,
                               DatabaseActivationPort activationPort, Clock clock) {
        this.repository = repository;
        this.probe = probe;
        this.activationPort = activationPort;
        this.clock = clock;
    }

    @Override
    public DbConnectionResult register(RegisterDbConnectionCommand command) {
        DbConnection connection = DbConnection.register(Ids.newId(), command.name(), command.vendor(), command.host(),
                command.port(), command.database(), command.username(), command.password(), now());
        repository.save(connection);
        return DbConnectionResult.from(connection);
    }

    @Override
    public DbConnectionResult update(UpdateDbConnectionCommand command) {
        DbConnection connection = load(command.id());
        connection.changeSettings(command.name(), command.vendor(), command.host(), command.port(), command.database(),
                command.username(), command.password(), now());
        repository.save(connection);
        return DbConnectionResult.from(connection);
    }

    @Override
    public DbConnectionResult test(String id) {
        DbConnection connection = load(id);
        ProbeResult result = probe.probe(connection);
        if (result.success()) {
            connection.recordSuccess(result.message(), now());
        } else {
            connection.recordFailure(result.message(), now());
        }
        repository.save(connection);
        return DbConnectionResult.from(connection);
    }

    @Override
    public DbConnectionResult activate(String id, Actor actor) {
        DbConnection target = load(id);
        ProbeResult result = probe.probe(target);
        Instant now = now();
        if (!result.success()) {
            target.recordFailure(result.message(), now);
            repository.save(target);
            throw new BusinessException(ErrorCode.DB_CONNECTION_FAILED, result.message());
        }
        target.recordSuccess(result.message(), now);
        activationPort.activate(target, actor);
        List<DbConnection> all = repository.findAll();
        for (DbConnection connection : all) {
            if (connection.id().equals(target.id())) {
                continue;
            }
            connection.deactivate(now);
        }
        target.activate(now);
        List<DbConnection> updated = all.stream()
                .map(connection -> connection.id().equals(target.id()) ? target : connection)
                .toList();
        repository.saveAll(updated);
        return DbConnectionResult.from(target);
    }

    @Override
    public DbConnectionResult get(String id) {
        return DbConnectionResult.from(load(id));
    }

    @Override
    public List<DbConnectionResult> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(DbConnection::createdAt))
                .map(DbConnectionResult::from)
                .toList();
    }

    @Override
    public Optional<DbConnectionResult> active() {
        return repository.findAll().stream().filter(DbConnection::active).findFirst().map(DbConnectionResult::from);
    }

    private DbConnection load(String id) {
        return repository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.DB_CONNECTION_NOT_FOUND, id));
    }

    private Instant now() {
        return clock.instant();
    }
}
