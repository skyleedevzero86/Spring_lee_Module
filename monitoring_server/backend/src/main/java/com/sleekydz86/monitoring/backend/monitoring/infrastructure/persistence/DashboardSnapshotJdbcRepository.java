package com.sleekydz86.monitoring.backend.monitoring.infrastructure.persistence;

import com.sleekydz86.monitoring.backend.monitoring.application.support.DashboardWindow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class DashboardSnapshotJdbcRepository {

    public record DashboardSnapshotRecord(
            Instant capturedAt,
            int requestPerMinute,
            double latencyMs,
            int cacheHitRate,
            int activeSessions,
            int alertQueue,
            double hostMemoryUsagePercent,
            double hostDiskUsagePercent,
            long hostUsedMemoryBytes,
            long hostTotalMemoryBytes,
            long hostUsedDiskBytes,
            long hostTotalDiskBytes,
            Long postgresqlUsedBytes,
            Long redisUsedBytes,
            Long redisLimitBytes,
            double databaseAvailability,
            double databaseLatencyMs,
            double redisAvailability,
            double redisLatencyMs
    ) {
    }

    public record TrendBucket(
            Instant capturedAt,
            double requestPerMinute,
            double latencyMs,
            double cacheHitRate
    ) {
    }

    public record StoreBaseline(
            Long postgresqlUsedBytes,
            Long redisUsedBytes
    ) {
    }

    private final JdbcTemplate jdbcTemplate;

    public DashboardSnapshotJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(DashboardSnapshotRecord record) {
        this.jdbcTemplate.update("""
                INSERT INTO monitoring_dashboard_snapshot (
                    captured_at,
                    request_per_minute,
                    latency_ms,
                    cache_hit_rate,
                    active_sessions,
                    alert_queue,
                    host_memory_usage_percent,
                    host_disk_usage_percent,
                    host_used_memory_bytes,
                    host_total_memory_bytes,
                    host_used_disk_bytes,
                    host_total_disk_bytes,
                    postgresql_used_bytes,
                    redis_used_bytes,
                    redis_limit_bytes,
                    db_availability,
                    db_latency_ms,
                    redis_availability,
                    redis_latency_ms
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                Timestamp.from(record.capturedAt()),
                record.requestPerMinute(),
                record.latencyMs(),
                record.cacheHitRate(),
                record.activeSessions(),
                record.alertQueue(),
                record.hostMemoryUsagePercent(),
                record.hostDiskUsagePercent(),
                record.hostUsedMemoryBytes(),
                record.hostTotalMemoryBytes(),
                record.hostUsedDiskBytes(),
                record.hostTotalDiskBytes(),
                record.postgresqlUsedBytes(),
                record.redisUsedBytes(),
                record.redisLimitBytes(),
                record.databaseAvailability(),
                record.databaseLatencyMs(),
                record.redisAvailability(),
                record.redisLatencyMs()
        );
    }

    public List<TrendBucket> findTrendBuckets(DashboardWindow window) {
        Instant now = Instant.now();
        List<TrendBucket> buckets = this.jdbcTemplate.query("""
                SELECT date_bin(CAST(? AS interval), captured_at, TIMESTAMPTZ '2001-01-01 00:00:00+00') AS bucket,
                       AVG(request_per_minute) AS request_per_minute,
                       AVG(latency_ms) AS latency_ms,
                       AVG(cache_hit_rate) AS cache_hit_rate
                FROM monitoring_dashboard_snapshot
                WHERE captured_at >= ?
                GROUP BY bucket
                ORDER BY bucket
                """,
                (rs, rowNum) -> new TrendBucket(
                        rs.getTimestamp("bucket").toInstant(),
                        rs.getDouble("request_per_minute"),
                        rs.getDouble("latency_ms"),
                        rs.getDouble("cache_hit_rate")
                ),
                window.bucketInterval(),
                Timestamp.from(window.since(now))
        );

        if (!buckets.isEmpty()) {
            return buckets;
        }

        List<TrendBucket> latest = new ArrayList<>(this.jdbcTemplate.query("""
                SELECT captured_at,
                       request_per_minute,
                       latency_ms,
                       cache_hit_rate
                FROM monitoring_dashboard_snapshot
                ORDER BY captured_at DESC
                LIMIT 12
                """,
                (rs, rowNum) -> new TrendBucket(
                        rs.getTimestamp("captured_at").toInstant(),
                        rs.getDouble("request_per_minute"),
                        rs.getDouble("latency_ms"),
                        rs.getDouble("cache_hit_rate")
                )
        ));
        Collections.reverse(latest);
        return latest;
    }

    public StoreBaseline findStoreBaseline(DashboardWindow window) {
        List<StoreBaseline> rows = this.jdbcTemplate.query("""
                SELECT postgresql_used_bytes,
                       redis_used_bytes
                FROM monitoring_dashboard_snapshot
                WHERE captured_at >= ?
                ORDER BY captured_at ASC
                LIMIT 1
                """,
                (rs, rowNum) -> new StoreBaseline(
                        rs.getObject("postgresql_used_bytes", Long.class),
                        rs.getObject("redis_used_bytes", Long.class)
                ),
                Timestamp.from(window.since(Instant.now()))
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }
}
