package com.sleekydz86.storagemigration.global.jdbc;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    public void ensureSchema(JdbcTemplate jdbcTemplate, SqlDialect dialect) {
        for (Map.Entry<String, String> entry : SchemaDefinition.createStatements(dialect).entrySet()) {
            if (!exists(jdbcTemplate, entry.getKey())) {
                jdbcTemplate.execute(entry.getValue());
                log.info("테이블을 만들었습니다: {}", entry.getKey());
            }
        }
        for (String statement : ProcedureDefinition.statements(dialect)) {
            jdbcTemplate.execute(statement);
        }
        log.info("{} 프로시저 준비 완료: {}", dialect, ProcedureNames.ALL);
    }

    private boolean exists(JdbcTemplate jdbcTemplate, String table) {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE 1 = 0", Integer.class);
            return true;
        } catch (DataAccessException exception) {
            return false;
        }
    }
}
