package com.sleekydz86.storagemigration.connection.domain;

import com.sleekydz86.storagemigration.global.error.BusinessException;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.util.Require;
import com.sleekydz86.storagemigration.global.util.Texts;
import java.time.Instant;

public class DbConnection {

    private final String id;
    private String name;
    private DbVendor vendor;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private ConnectionStatus status;
    private String lastMessage;
    private Instant lastCheckedAt;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;

    private DbConnection(String id, String name, DbVendor vendor, String host, int port, String database, String username,
                         String password, ConnectionStatus status, String lastMessage, Instant lastCheckedAt,
                         boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.vendor = vendor;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.status = status;
        this.lastMessage = lastMessage;
        this.lastCheckedAt = lastCheckedAt;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DbConnection register(String id, String name, DbVendor vendor, String host, Integer port, String database,
                                        String username, String password, Instant now) {
        DbVendor checkedVendor = Require.notNull(vendor, "DB 종류");
        return new DbConnection(Require.notBlank(id, "id"),
                Require.maxLength(Require.notBlank(name, "연결 이름"), 100, "연결 이름"),
                checkedVendor,
                Require.notBlank(host, "호스트"),
                Require.port(port, checkedVendor.defaultPort()),
                Require.notBlank(database, "DB 이름(서비스명)"),
                Require.notBlank(username, "아이디"),
                Require.notBlank(password, "비밀번호"),
                ConnectionStatus.UNTESTED, null, null, false, now, now);
    }

    public static DbConnection restore(String id, String name, DbVendor vendor, String host, int port, String database,
                                       String username, String password, ConnectionStatus status, String lastMessage,
                                       Instant lastCheckedAt, boolean active, Instant createdAt, Instant updatedAt) {
        return new DbConnection(id, name, vendor, host, port, database, username, password, status, lastMessage,
                lastCheckedAt, active, createdAt, updatedAt);
    }

    public void changeSettings(String name, DbVendor vendor, String host, Integer port, String database, String username,
                               String newPassword, Instant now) {
        DbVendor checkedVendor = Require.notNull(vendor, "DB 종류");
        this.name = Require.maxLength(Require.notBlank(name, "연결 이름"), 100, "연결 이름");
        this.vendor = checkedVendor;
        this.host = Require.notBlank(host, "호스트");
        this.port = Require.port(port, checkedVendor.defaultPort());
        this.database = Require.notBlank(database, "DB 이름(서비스명)");
        if (Texts.hasText(username)) {
            this.username = username.trim();
        }
        if (Texts.hasText(newPassword)) {
            this.password = newPassword;
        }
        this.status = ConnectionStatus.UNTESTED;
        this.lastMessage = null;
        this.active = false;
        this.updatedAt = now;
    }

    public void recordSuccess(String message, Instant at) {
        this.status = ConnectionStatus.CONNECTED;
        this.lastMessage = message;
        this.lastCheckedAt = at;
        this.updatedAt = at;
    }

    public void recordFailure(String message, Instant at) {
        this.status = ConnectionStatus.FAILED;
        this.lastMessage = message;
        this.lastCheckedAt = at;
        this.active = false;
        this.updatedAt = at;
    }

    public void activate(Instant at) {
        if (status != ConnectionStatus.CONNECTED) {
            throw new BusinessException(ErrorCode.DB_CONNECTION_NOT_TESTED);
        }
        this.active = true;
        this.updatedAt = at;
    }

    public void deactivate(Instant at) {
        if (active) {
            this.active = false;
            this.updatedAt = at;
        }
    }

    public String jdbcUrl() {
        return vendor.jdbcUrl(host, port, database);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public DbVendor vendor() {
        return vendor;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String database() {
        return database;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public ConnectionStatus status() {
        return status;
    }

    public String lastMessage() {
        return lastMessage;
    }

    public Instant lastCheckedAt() {
        return lastCheckedAt;
    }

    public boolean active() {
        return active;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
