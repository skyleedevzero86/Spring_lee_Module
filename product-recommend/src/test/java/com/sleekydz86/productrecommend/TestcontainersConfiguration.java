package com.sleekydz86.productrecommend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	ElasticsearchContainer elasticsearchContainer() {
		return new ElasticsearchContainer(
			DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.15.3")
		);
	}

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(
			DockerImageName.parse("pgvector/pgvector:pg17").asCompatibleSubstituteFor("postgres")
		);
	}
}
