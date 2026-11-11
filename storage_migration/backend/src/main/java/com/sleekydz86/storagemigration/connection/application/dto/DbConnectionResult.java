package com.sleekydz86.storagemigration.connection.application.dto;

import com.sleekydz86.storagemigration.connection.domain.ConnectionStatus;
import com.sleekydz86.storagemigration.connection.domain.DbConnection;
import com.sleekydz86.storagemigration.connection.domain.DbVendor;
import com.sleekydz86.storagemigration.global.util.Texts;
import java.time.Instant;

public record DbConnectionResult(String id, String name, DbVendor vendor, String host, int port, String database,
                                 String maskedUsername, ConnectionStatus status, String lastMessage,
                                 Instant lastCheckedAt, boolean active, Instant createdAt, Instant updatedAt) {

    public static DbConnectionResult from(DbConnection connection) {
        return new DbConnectionResult(connection.id(), connection.name(), connection.vendor(), connection.host(),
                connection.port(), connection.database(), Texts.mask(connection.username()), connection.status(),
                connection.lastMessage(), connection.lastCheckedAt(), connection.active(), connection.createdAt(),
                connection.updatedAt());
    }
}
