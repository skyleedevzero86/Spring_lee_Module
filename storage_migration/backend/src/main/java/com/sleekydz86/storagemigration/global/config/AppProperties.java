package com.sleekydz86.storagemigration.global.config;

import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Path home, String cryptoSecret, long maxDbContentBytes, List<String> allowedOrigins) {

    private static final long DEFAULT_MAX_DB_CONTENT_BYTES = 50L * 1024 * 1024;

    public AppProperties {
        if (home == null) {
            home = Path.of(System.getProperty("user.home"), ".storage-migration");
        }
        if (maxDbContentBytes <= 0) {
            maxDbContentBytes = DEFAULT_MAX_DB_CONTENT_BYTES;
        }
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            allowedOrigins = List.of("http://localhost:5173");
        }
    }
}
