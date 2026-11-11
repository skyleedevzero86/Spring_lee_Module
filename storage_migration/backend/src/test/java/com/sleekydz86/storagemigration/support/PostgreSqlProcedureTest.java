package com.sleekydz86.storagemigration.support;

import com.sleekydz86.storagemigration.global.jdbc.SqlDialect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@DisplayName("PostgreSQL 프로시저·BLOB 실제 연동")
@EnabledIfEnvironmentVariable(named = "SM_IT_PG_URL", matches = ".+")
class PostgreSqlProcedureTest extends DatabaseProcedureTestSupport {

    @Override
    protected SqlDialect dialect() {
        return SqlDialect.POSTGRESQL;
    }

    @Override
    protected String prefix() {
        return "SM_IT_PG";
    }
}
