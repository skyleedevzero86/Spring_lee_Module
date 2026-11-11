package com.sleekydz86.storagemigration.global.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProcedureDefinition {

    private static final String LOCATION = "db/procedure/%s/%s.sql";

    private ProcedureDefinition() {
    }

    public static List<String> statements(SqlDialect dialect) {
        List<String> statements = new ArrayList<>();
        for (String name : ProcedureNames.ALL) {
            if (dialect == SqlDialect.MYSQL) {
                statements.add("DROP PROCEDURE IF EXISTS " + name);
            }
            statements.add(load(dialect, name));
        }
        return statements;
    }

    static String load(SqlDialect dialect, String name) {
        String path = LOCATION.formatted(dialect.name().toLowerCase(Locale.ROOT), name);
        try (InputStream input = ProcedureDefinition.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("프로시저 파일이 없습니다: " + path);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
