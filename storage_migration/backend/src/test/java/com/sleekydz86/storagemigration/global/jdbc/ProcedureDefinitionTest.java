package com.sleekydz86.storagemigration.global.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("프로시저 정의")
class ProcedureDefinitionTest {

    @ParameterizedTest(name = "{0}")
    @EnumSource(SqlDialect.class)
    @DisplayName("DB마다 모든 프로시저 파일이 있고 rtn_cod·err_msg를 돌려준다")
    void everyProcedureHasFileForEveryDatabase(SqlDialect dialect) {
        // given
        List<String> names = ProcedureNames.ALL;

        // when
        List<String> sources = names.stream().map(name -> ProcedureDefinition.load(dialect, name)).toList();

        // then
        for (int index = 0; index < names.size(); index++) {
            assertThat(sources.get(index)).contains("PROCEDURE " + names.get(index)).contains("rtn_cod")
                    .contains("err_msg");
        }
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(SqlDialect.class)
    @DisplayName("등록·수정·삭제·BLOB 저장 프로시저는 작업 로그를 남긴다")
    void cudProceduresWriteAuditLog(SqlDialect dialect) {
        // given
        List<String> names = List.of(ProcedureNames.STORAGE_SERVER_CUD, ProcedureNames.STORED_FILE_CUD,
                ProcedureNames.STORED_FILE_CONTENT_U);

        // when
        List<String> sources = names.stream().map(name -> ProcedureDefinition.load(dialect, name)).toList();

        // then
        assertThat(sources).allSatisfy(sql -> assertThat(sql).contains("INSERT INTO SM_AUDIT_LOG")
                .contains("in_worker").contains("in_workip"));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(SqlDialect.class)
    @DisplayName("프로시저 오류 메시지는 한국어다")
    void errorMessagesAreKorean(SqlDialect dialect) {
        // given
        String source = ProcedureDefinition.load(dialect, ProcedureNames.STORED_FILE_CUD);

        // when
        boolean hasEnglishMessage = source.contains("not found") || source.contains("is empty");

        // then
        assertThat(hasEnglishMessage).isFalse();
        assertThat(source).contains("파일 정보를 찾을 수 없습니다").contains("DB 오류: ");
    }

    @Test
    @DisplayName("MySQL은 프로시저를 다시 만들기 전에 먼저 지운다")
    void mysqlDropsBeforeCreate() {
        // given
        SqlDialect dialect = SqlDialect.MYSQL;

        // when
        List<String> statements = ProcedureDefinition.statements(dialect);

        // then
        assertThat(statements).hasSize(ProcedureNames.ALL.size() * 2);
        assertThat(statements.getFirst()).isEqualTo("DROP PROCEDURE IF EXISTS " + ProcedureNames.ALL.getFirst());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(SqlDialect.class)
    @DisplayName("모든 테이블에 작업자·IP·일시 컬럼이 있다")
    void auditColumnsExistOnEveryTable(SqlDialect dialect) {
        // given / when
        Map<String, String> statements = SchemaDefinition.createStatements(dialect);

        // then
        assertThat(statements.values())
                .allSatisfy(sql -> assertThat(sql).contains("WORKER").contains("WORK_IP").contains("WORK_DT"));
    }
}
