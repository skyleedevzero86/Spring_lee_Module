package com.sleekydz86.storagemigration.global.jdbc;

import java.util.List;

public final class ProcedureNames {

    public static final String STORAGE_SERVER_CUD = "SP_SM_STORAGE_SERVER_CUD";
    public static final String STORED_FILE_CUD = "SP_SM_STORED_FILE_CUD";
    public static final String STORED_FILE_CONTENT_U = "SP_SM_STORED_FILE_CONTENT_U";
    public static final String AUDIT_LOG_C = "SP_SM_AUDIT_LOG_C";

    public static final List<String> ALL = List.of(STORAGE_SERVER_CUD, STORED_FILE_CUD, STORED_FILE_CONTENT_U,
            AUDIT_LOG_C);

    private ProcedureNames() {
    }
}
