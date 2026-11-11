package com.sleekydz86.storagemigration.global.jdbc;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SchemaDefinition {

    public static final String STORAGE_SERVER_TABLE = "SM_STORAGE_SERVER";
    public static final String STORED_FILE_TABLE = "SM_STORED_FILE";
    public static final String AUDIT_LOG_TABLE = "SM_AUDIT_LOG";

    private SchemaDefinition() {
    }

    public static Map<String, String> createStatements(SqlDialect dialect) {
        Map<String, String> statements = new LinkedHashMap<>();
        statements.put(STORAGE_SERVER_TABLE, "CREATE TABLE " + STORAGE_SERVER_TABLE + " ("
                + "ID " + dialect.varchar(36) + " NOT NULL PRIMARY KEY, "
                + "NAME " + dialect.varchar(100) + " NOT NULL, "
                + "STORAGE_TYPE " + dialect.varchar(20) + " NOT NULL, "
                + "ENDPOINT " + dialect.varchar(500) + ", "
                + "REGION " + dialect.varchar(50) + " NOT NULL, "
                + "BUCKET " + dialect.varchar(100) + " NOT NULL, "
                + "ACCESS_KEY " + dialect.varchar(1000) + " NOT NULL, "
                + "SECRET_KEY " + dialect.varchar(1000) + " NOT NULL, "
                + "STATUS " + dialect.varchar(20) + " NOT NULL, "
                + "LAST_MESSAGE " + dialect.varchar(1000) + ", "
                + "LAST_CHECKED_AT " + dialect.timestamp() + ", "
                + "CREATED_AT " + dialect.timestamp() + " NOT NULL, "
                + "UPDATED_AT " + dialect.timestamp() + " NOT NULL, "
                + auditColumns(dialect)
                + ")" + dialect.tableSuffix());
        statements.put(STORED_FILE_TABLE, "CREATE TABLE " + STORED_FILE_TABLE + " ("
                + "ID " + dialect.varchar(36) + " NOT NULL PRIMARY KEY, "
                + "STORAGE_SERVER_ID " + dialect.varchar(36) + " NOT NULL, "
                + "BUCKET " + dialect.varchar(100) + " NOT NULL, "
                + "OBJECT_KEY " + dialect.varchar(1000) + " NOT NULL, "
                + "ORIGINAL_NAME " + dialect.varchar(500) + " NOT NULL, "
                + "CONTENT_TYPE " + dialect.varchar(200) + ", "
                + "FILE_SIZE " + dialect.bigint() + " NOT NULL, "
                + "CHECKSUM " + dialect.varchar(64) + ", "
                + "TITLE " + dialect.varchar(200) + ", "
                + "DESCRIPTION " + dialect.varchar(2000) + ", "
                + "TAGS " + dialect.varchar(500) + ", "
                + "STATUS " + dialect.varchar(20) + " NOT NULL, "
                + "CONTENT " + dialect.blob() + ", "
                + "UPLOADED_AT " + dialect.timestamp() + " NOT NULL, "
                + "SAVED_AT " + dialect.timestamp() + ", "
                + "UPDATED_AT " + dialect.timestamp() + " NOT NULL, "
                + auditColumns(dialect)
                + ")" + dialect.tableSuffix());
        statements.put(AUDIT_LOG_TABLE, "CREATE TABLE " + AUDIT_LOG_TABLE + " ("
                + "LOG_ID " + dialect.identity() + ", "
                + "ACTION " + dialect.varchar(50) + " NOT NULL, "
                + "TARGET_TYPE " + dialect.varchar(30) + " NOT NULL, "
                + "TARGET_ID " + dialect.varchar(36) + ", "
                + "DETAIL " + dialect.varchar(1000) + ", "
                + auditColumns(dialect)
                + ")" + dialect.tableSuffix());
        return statements;
    }

    private static String auditColumns(SqlDialect dialect) {
        return "WORKER " + dialect.varchar(100) + ", "
                + "WORK_IP " + dialect.varchar(50) + ", "
                + "WORK_DT " + dialect.timestamp() + " NOT NULL";
    }
}
