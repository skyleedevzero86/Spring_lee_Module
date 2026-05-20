package com.sleekydz86.monitoring.logstack_s3.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
public abstract class IntegrationTestBase {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"));

    @Container
    @SuppressWarnings("resource")
    static final LocalStackContainer LOCALSTACK = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.0"))
            .withServices(LocalStackContainer.Service.S3);

    @LocalServerPort
    protected int port;

    protected final RestTemplate restTemplate = new RestTemplate();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("aws.s3.endpoint", () -> LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        registry.add("aws.s3.region", LOCALSTACK::getRegion);
        registry.add("aws.s3.access-key", LOCALSTACK::getAccessKey);
        registry.add("aws.s3.secret-key", LOCALSTACK::getSecretKey);
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }
}
