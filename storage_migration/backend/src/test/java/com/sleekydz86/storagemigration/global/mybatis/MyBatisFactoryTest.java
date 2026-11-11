package com.sleekydz86.storagemigration.global.mybatis;

import static org.assertj.core.api.Assertions.assertThat;

import com.sleekydz86.storagemigration.global.jdbc.SqlDialect;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@DisplayName("MyBatis 매퍼")
class MyBatisFactoryTest {

    private static final String FILE_MAPPER = "com.sleekydz86.storagemigration.file.adapter.out.persistence.StoredFileMapper";
    private static final String SERVER_MAPPER =
            "com.sleekydz86.storagemigration.storage.adapter.out.persistence.StorageServerMapper";
    private static final String AUDIT_MAPPER = "com.sleekydz86.storagemigration.audit.adapter.out.persistence.AuditLogMapper";

    @ParameterizedTest(name = "{0}")
    @EnumSource(SqlDialect.class)
    @DisplayName("모든 매퍼 XML을 읽고, CUD는 프로시저 호출(CALLABLE)이다")
    void parsesEveryMapperForEveryDatabase(SqlDialect dialect) {
        // given
        Configuration configuration = configuration(dialect);

        // when
        MappedStatement cud = configuration.getMappedStatement(FILE_MAPPER + ".cud");
        BoundSql bound = cud.getBoundSql(null);

        // then
        assertThat(cud.getStatementType()).isEqualTo(StatementType.CALLABLE);
        assertThat(bound.getSql()).contains("SP_SM_STORED_FILE_CUD");
        assertThat(bound.getParameterMappings()).last()
                .satisfies(mapping -> assertThat(mapping.getMode()).isEqualTo(ParameterMode.INOUT));
        assertThat(configuration.hasStatement(FILE_MAPPER + ".saveContent")).isTrue();
        assertThat(configuration.hasStatement(SERVER_MAPPER + ".cud")).isTrue();
        assertThat(configuration.hasStatement(AUDIT_MAPPER + ".record")).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(SqlDialect.class)
    @DisplayName("BLOB 길이 조회는 DB에 맞는 함수를 쓴다")
    void contentLengthUsesDatabaseFunction(SqlDialect dialect) {
        // given
        Configuration configuration = configuration(dialect);

        // when
        String sql = configuration.getMappedStatement(FILE_MAPPER + ".contentLength").getBoundSql("id").getSql();

        // then
        if (dialect == SqlDialect.ORACLE) {
            assertThat(sql).contains("DBMS_LOB.GETLENGTH");
        } else {
            assertThat(sql).contains("LENGTH(CONTENT)").doesNotContain("DBMS_LOB");
        }
    }

    private static Configuration configuration(SqlDialect dialect) {
        return MyBatisFactory.build(new DriverManagerDataSource("jdbc:unused"), dialect).getConfiguration();
    }
}
