package com.ja_elk.config;

import org.fluentd.logger.FluentLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FluentdConfig {

    @Bean
    public FluentLogger fluentLogger() {
        return FluentLogger.getLogger("spring", "localhost", 24224);
    }
}