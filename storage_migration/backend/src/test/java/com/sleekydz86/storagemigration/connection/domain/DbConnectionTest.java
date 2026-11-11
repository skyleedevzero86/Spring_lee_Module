package com.sleekydz86.storagemigration.connection.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sleekydz86.storagemigration.global.error.BusinessException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DB 연결")
class DbConnectionTest {

    private static final Instant NOW = Instant.parse("2026-11-11T00:00:00Z");

    @Test
    @DisplayName("포트를 비우면 DB 기본 포트로 JDBC 주소를 만든다")
    void usesVendorDefaultPortAndUrl() {
        // given
        Integer port = null;

        // when
        DbConnection connection = DbConnection.register("1", "local", DbVendor.POSTGRESQL, "localhost", port, "app",
                "user", "pw", NOW);

        // then
        assertThat(connection.port()).isEqualTo(5432);
        assertThat(connection.jdbcUrl()).startsWith("jdbc:postgresql://localhost:5432/app");
    }

    @Test
    @DisplayName("연결 테스트에 성공해야 사용할 수 있다")
    void cannotActivateBeforeSuccessfulTest() {
        // given
        DbConnection connection = DbConnection.register("1", "local", DbVendor.MYSQL, "localhost", null, "app",
                "user", "pw", NOW);

        // when / then
        assertThatThrownBy(() -> connection.activate(NOW)).isInstanceOf(BusinessException.class);

        // when
        connection.recordSuccess("MySQL", NOW);
        connection.activate(NOW);

        // then
        assertThat(connection.active()).isTrue();
    }

    @Test
    @DisplayName("접속 정보를 바꾸면 사용을 멈추고 다시 테스트해야 한다")
    void changingSettingsRequiresNewTest() {
        // given
        DbConnection connection = DbConnection.register("1", "local", DbVendor.MYSQL, "localhost", 3306, "app",
                "user", "pw", NOW);
        connection.recordSuccess("MySQL", NOW);
        connection.activate(NOW);

        // when
        connection.changeSettings("local", DbVendor.MYSQL, "db.internal", 3306, "app", "", null, NOW);

        // then
        assertThat(connection.status()).isEqualTo(ConnectionStatus.UNTESTED);
        assertThat(connection.active()).isFalse();
        assertThat(connection.username()).isEqualTo("user");
        assertThat(connection.password()).isEqualTo("pw");
    }
}
