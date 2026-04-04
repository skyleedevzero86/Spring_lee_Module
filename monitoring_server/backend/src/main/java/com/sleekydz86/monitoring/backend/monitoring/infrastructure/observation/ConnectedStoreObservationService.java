package com.sleekydz86.monitoring.backend.monitoring.infrastructure.observation;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ConnectedStoreObservationService {

    public record StoreSnapshot(
            String component,
            String status,
            String version,
            Long usedBytes,
            Long limitBytes,
            Long freeBytes,
            Double usagePercent,
            long dailyGrowthBytes,
            Instant capturedAt,
            String detail
    ) {
    }

    private record UsagePoint(Instant capturedAt, long usedBytes) {
    }

    private final JdbcTemplate jdbcTemplate;
    private final RedisConnectionFactory redisConnectionFactory;

    private final AtomicReference<String> postgresStatus = new AtomicReference<>("UNKNOWN");
    private final AtomicReference<String> postgresVersion = new AtomicReference<>("unknown");
    private final AtomicReference<String> postgresDetail = new AtomicReference<>("Waiting for the first measurement.");
    private final AtomicReference<Instant> postgresCapturedAt = new AtomicReference<>(Instant.EPOCH);
    private final AtomicLong postgresUsedBytes = new AtomicLong();
    private final AtomicLong postgresDailyGrowthBytes = new AtomicLong();

    private final AtomicReference<String> redisStatus = new AtomicReference<>("UNKNOWN");
    private final AtomicReference<String> redisVersion = new AtomicReference<>("unknown");
    private final AtomicReference<String> redisDetail = new AtomicReference<>("Waiting for the first measurement.");
    private final AtomicReference<Instant> redisCapturedAt = new AtomicReference<>(Instant.EPOCH);
    private final AtomicLong redisUsedBytes = new AtomicLong();
    private final AtomicLong redisLimitBytes = new AtomicLong(-1L);
    private final AtomicLong redisDailyGrowthBytes = new AtomicLong();

    private final ConcurrentLinkedDeque<UsagePoint> postgresHistory = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<UsagePoint> redisHistory = new ConcurrentLinkedDeque<>();

    public ConnectedStoreObservationService(
            ObjectProvider<JdbcTemplate> jdbcTemplate,
            ObjectProvider<RedisConnectionFactory> redisConnectionFactory,
            MeterRegistry meterRegistry
    ) {
        this.jdbcTemplate = jdbcTemplate.getIfAvailable();
        this.redisConnectionFactory = redisConnectionFactory.getIfAvailable();

        Gauge.builder("monitoring.storage.used", this.postgresUsedBytes, AtomicLong::get)
                .description("Current storage used by a connected store")
                .baseUnit("bytes")
                .tag("component", "postgresql")
                .register(meterRegistry);
        Gauge.builder("monitoring.storage.used", this.redisUsedBytes, AtomicLong::get)
                .description("Current storage used by a connected store")
                .baseUnit("bytes")
                .tag("component", "redis")
                .register(meterRegistry);
        Gauge.builder("monitoring.storage.limit", () -> normalizeLimit(this.redisLimitBytes.get()))
                .description("Configured storage limit of a connected store when available")
                .baseUnit("bytes")
                .tag("component", "redis")
                .register(meterRegistry);
        Gauge.builder("monitoring.storage.daily.growth", this.postgresDailyGrowthBytes, AtomicLong::get)
                .description("Net storage growth over the recent 24 hours")
                .baseUnit("bytes")
                .tag("component", "postgresql")
                .register(meterRegistry);
        Gauge.builder("monitoring.storage.daily.growth", this.redisDailyGrowthBytes, AtomicLong::get)
                .description("Net storage growth over the recent 24 hours")
                .baseUnit("bytes")
                .tag("component", "redis")
                .register(meterRegistry);

        refresh();
    }

    @Scheduled(fixedDelayString = "${idolglow.monitoring.store-sample-interval-ms:60000}", initialDelay = 6000)
    public void refresh() {
        refreshPostgreSql();
        refreshRedis();
    }

    public List<StoreSnapshot> snapshots() {
        List<StoreSnapshot> snapshots = new ArrayList<>();
        snapshots.add(new StoreSnapshot(
                "PostgreSQL",
                this.postgresStatus.get(),
                this.postgresVersion.get(),
                this.postgresUsedBytes.get(),
                null,
                null,
                null,
                this.postgresDailyGrowthBytes.get(),
                this.postgresCapturedAt.get(),
                this.postgresDetail.get()
        ));
        snapshots.add(new StoreSnapshot(
                "Redis",
                this.redisStatus.get(),
                this.redisVersion.get(),
                this.redisUsedBytes.get(),
                nullableLimit(this.redisLimitBytes.get()),
                calculateFreeBytes(this.redisUsedBytes.get(), nullableLimit(this.redisLimitBytes.get())),
                calculateUsagePercent(this.redisUsedBytes.get(), nullableLimit(this.redisLimitBytes.get())),
                this.redisDailyGrowthBytes.get(),
                this.redisCapturedAt.get(),
                this.redisDetail.get()
        ));
        return snapshots;
    }

    private void refreshPostgreSql() {
        Instant now = Instant.now();
        try {
            if (this.jdbcTemplate == null) {
                this.postgresStatus.set("UNKNOWN");
                this.postgresDetail.set("JdbcTemplate bean is not available.");
                this.postgresCapturedAt.set(now);
                return;
            }

            Map<String, Object> row = this.jdbcTemplate.queryForMap("""
                    SELECT current_database() AS database_name,
                           current_setting('server_version') AS server_version,
                           pg_database_size(current_database()) AS used_bytes
                    """);

            String databaseName = stringValue(row.get("database_name"), "unknown");
            String serverVersion = stringValue(row.get("server_version"), "unknown");
            long usedBytes = longValue(row.get("used_bytes"));

            this.postgresStatus.set("UP");
            this.postgresVersion.set(serverVersion);
            this.postgresUsedBytes.set(usedBytes);
            this.postgresCapturedAt.set(now);

            recordUsagePoint(this.postgresHistory, now, usedBytes);
            this.postgresDailyGrowthBytes.set(calculateDailyGrowth(this.postgresHistory, now, usedBytes));
            this.postgresDetail.set("database=" + databaseName + ", recent 24h delta is calculated from in-app snapshots.");
        }
        catch (Exception exception) {
            this.postgresStatus.set("DOWN");
            this.postgresCapturedAt.set(now);
            this.postgresDetail.set(compactMessage(exception));
        }
    }

    private void refreshRedis() {
        Instant now = Instant.now();
        try {
            if (this.redisConnectionFactory == null) {
                this.redisStatus.set("UNKNOWN");
                this.redisDetail.set("RedisConnectionFactory bean is not available.");
                this.redisCapturedAt.set(now);
                return;
            }

            RedisConnection connection = this.redisConnectionFactory.getConnection();
            try {
                Properties memory = connection.info("memory");
                Properties server = connection.info("server");
                Properties stats = connection.info("stats");

                long usedBytes = longValue(memory.getProperty("used_memory"));
                long limitBytes = longValue(memory.getProperty("maxmemory"));
                String version = stringValue(server.getProperty("redis_version"), "unknown");
                String os = stringValue(server.getProperty("os"), "unknown");
                String hits = stringValue(stats.getProperty("keyspace_hits"), "0");
                String misses = stringValue(stats.getProperty("keyspace_misses"), "0");

                this.redisStatus.set("UP");
                this.redisVersion.set(version);
                this.redisUsedBytes.set(usedBytes);
                this.redisLimitBytes.set(limitBytes > 0L ? limitBytes : -1L);
                this.redisCapturedAt.set(now);

                recordUsagePoint(this.redisHistory, now, usedBytes);
                this.redisDailyGrowthBytes.set(calculateDailyGrowth(this.redisHistory, now, usedBytes));

                String limitMessage = limitBytes > 0L
                        ? "maxmemory is configured."
                        : "No maxmemory limit is configured.";
                this.redisDetail.set("os=" + os + ", hits=" + hits + ", misses=" + misses + ", " + limitMessage);
            }
            finally {
                connection.close();
            }
        }
        catch (Exception exception) {
            this.redisStatus.set("DOWN");
            this.redisCapturedAt.set(now);
            this.redisDetail.set(compactMessage(exception));
        }
    }

    private static void recordUsagePoint(ConcurrentLinkedDeque<UsagePoint> history, Instant capturedAt, long usedBytes) {
        history.addLast(new UsagePoint(capturedAt, usedBytes));
        Instant expiration = capturedAt.minus(Duration.ofHours(30));
        while (!history.isEmpty()) {
            UsagePoint oldest = history.peekFirst();
            if (oldest == null || !oldest.capturedAt().isBefore(expiration)) {
                break;
            }
            history.pollFirst();
        }
    }

    private static long calculateDailyGrowth(ConcurrentLinkedDeque<UsagePoint> history, Instant now, long currentUsedBytes) {
        Instant cutoff = now.minus(Duration.ofHours(24));
        UsagePoint baseline = history.stream()
                .filter((point) -> !point.capturedAt().isBefore(cutoff))
                .findFirst()
                .orElse(history.peekFirst());

        if (baseline == null) {
            return 0L;
        }

        return currentUsedBytes - baseline.usedBytes();
    }

    private static Long nullableLimit(long value) {
        return value > 0L ? value : null;
    }

    private static long normalizeLimit(long value) {
        return value > 0L ? value : 0L;
    }

    private static Long calculateFreeBytes(long usedBytes, Long limitBytes) {
        if (limitBytes == null) {
            return null;
        }
        return Math.max(0L, limitBytes - usedBytes);
    }

    private static Double calculateUsagePercent(long usedBytes, Long limitBytes) {
        if (limitBytes == null || limitBytes <= 0L) {
            return null;
        }
        return (usedBytes * 100.0) / limitBytes;
    }

    private static long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(value.toString());
    }

    private static String stringValue(Object value, String fallback) {
        return value == null ? fallback : value.toString();
    }

    private static String compactMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        String compact = message.replaceAll("\\s+", " ").trim();
        if (compact.length() <= 110) {
            return exception.getClass().getSimpleName() + ": " + compact;
        }

        return exception.getClass().getSimpleName() + ": " + compact.substring(0, 107) + "...";
    }
}
