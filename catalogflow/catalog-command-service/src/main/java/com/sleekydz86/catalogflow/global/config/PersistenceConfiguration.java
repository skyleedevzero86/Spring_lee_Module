package com.sleekydz86.catalogflow.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.sleekydz86.catalogflow.adapter.out.persistence")
public class PersistenceConfiguration {
}
