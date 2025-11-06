package com.sleekydz86.ocrstudy1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "minio.endpoint=http://localhost:9000",
        "encryption.key=TestKey123456789012345678901234567890"
})
@DisplayName("Application Context 로딩 테스트")
class Ocrstudy01ApplicationTests {

    @Test
    @DisplayName("Spring Boot 애플리케이션 컨텍스트 로딩 성공")
    void contextLoads() {
        assertThat(true).isTrue();
    }
}