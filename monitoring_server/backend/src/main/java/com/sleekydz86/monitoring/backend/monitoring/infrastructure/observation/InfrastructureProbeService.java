package com.sleekydz86.monitoring.backend.monitoring.infrastructure.observation;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class InfrastructureProbeService {

    public record ProbeSnapshot(
            String component,
            String status,
            double availability,
            double latencyMs,
            Instant lastCheckedAt,
            String detail
    ) {
    }

    private final JdbcTemplate jdbcTemplate;
    private final RedisConnectionFactory redisConnectionFactory;

    private final AtomicInteger databaseAvailability = new AtomicInteger();
    private final AtomicInteger redisAvailability = new AtomicInteger();
    private final AtomicInteger databaseLatencyMs = new AtomicInteger();
    private final AtomicInteger redisLatencyMs = new AtomicInteger();
    private final AtomicReference<Instant> databaseCheckedAt = new AtomicReference<>(Instant.EPOCH);
    private final AtomicReference<Instant> redisCheckedAt = new AtomicReference<>(Instant.EPOCH);
    private final AtomicReference<String> databaseDetail = new AtomicReference<>("첫 프로브를 기다리는 중입니다.");
    private final AtomicReference<String> redisDetail = new AtomicReference<>("첫 프로브를 기다리는 중입니다.");

    private final Counter databaseFailures;
    private final Counter redisFailures;
    private final Timer databaseProbeTimer;
    private final Timer redisProbeTimer;

    public InfrastructureProbeService(
            ObjectProvider<JdbcTemplate> jdbcTemplate,
            ObjectProvider<RedisConnectionFactory> redisConnectionFactory,
            MeterRegistry meterRegistry
    ) {
        this.jdbcTemplate = jdbcTemplate.getIfAvailable();
        this.redisConnectionFactory = redisConnectionFactory.getIfAvailable();

        Gauge.builder("monitoring.infrastructure.availability", this.databaseAvailability, AtomicInteger::get)
                .description("1 when the dependency can be reached, otherwise 0")
                .tag("component", "db")
                .register(meterRegistry);
        Gauge.builder("monitoring.infrastructure.availability", this.redisAvailability, AtomicInteger::get)
                .description("1 when the dependency can be reached, otherwise 0")
                .tag("component", "redis")
                .register(meterRegistry);
        Gauge.builder("monitoring.infrastructure.last.latency", this.databaseLatencyMs, AtomicInteger::get)
                .description("Latest probe latency")
                .baseUnit("milliseconds")
                .tag("component", "db")
                .register(meterRegistry);
        Gauge.builder("monitoring.infrastructure.last.latency", this.redisLatencyMs, AtomicInteger::get)
                .description("Latest probe latency")
                .baseUnit("milliseconds")
                .tag("component", "redis")
                .register(meterRegistry);

        this.databaseFailures = Counter.builder("monitoring.infrastructure.probe.failures")
                .description("Number of failed infrastructure probes")
                .tag("component", "db")
                .register(meterRegistry);
        this.redisFailures = Counter.builder("monitoring.infrastructure.probe.failures")
                .description("Number of failed infrastructure probes")
                .tag("component", "redis")
                .register(meterRegistry);
        this.databaseProbeTimer = Timer.builder("monitoring.infrastructure.probe")
                .description("Latency of infrastructure dependency probes")
                .publishPercentiles(0.5, 0.9, 0.95)
                .tag("component", "db")
                .register(meterRegistry);
        this.redisProbeTimer = Timer.builder("monitoring.infrastructure.probe")
                .description("Latency of infrastructure dependency probes")
                .publishPercentiles(0.5, 0.9, 0.95)
                .tag("component", "redis")
                .register(meterRegistry);

        probeAll();
    }

    @Scheduled(fixedDelayString = "${idolglow.monitoring.probe-interval-ms:15000}", initialDelay = 5000)
    public void probeAll() {
        probeDatabase();
        probeRedis();
    }

    public List<ProbeSnapshot> snapshots() {
        return List.of(
                new ProbeSnapshot(
                        "PostgreSQL",
                        this.databaseAvailability.get() == 1 ? "UP" : "DOWN",
                        this.databaseAvailability.get(),
                        this.databaseLatencyMs.get(),
                        this.databaseCheckedAt.get(),
                        this.databaseDetail.get()
                ),
                new ProbeSnapshot(
                        "Redis",
                        this.redisAvailability.get() == 1 ? "UP" : "DOWN",
                        this.redisAvailability.get(),
                        this.redisLatencyMs.get(),
                        this.redisCheckedAt.get(),
                        this.redisDetail.get()
                )
        );
    }

    private void probeDatabase() {
        Instant startedAt = Instant.now();
        boolean available = false;

        try {
            if (this.jdbcTemplate == null) {
                this.databaseDetail.set("JdbcTemplate 빈을 찾을 수 없습니다.");
            }
            else {
                Integer result = this.jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                available = Integer.valueOf(1).equals(result);
                this.databaseDetail.set("SELECT 1 프로브에 성공했습니다.");
            }
        }
        catch (Exception exception) {
            this.databaseDetail.set(compactMessage(exception));
            this.databaseFailures.increment();
        }

        long elapsed = elapsedMillis(startedAt);
        this.databaseAvailability.set(available ? 1 : 0);
        this.databaseLatencyMs.set((int) elapsed);
        this.databaseCheckedAt.set(Instant.now());
        this.databaseProbeTimer.record(Duration.ofMillis(elapsed));
    }

    private void probeRedis() {
        Instant startedAt = Instant.now();
        boolean available = false;

        try {
            if (this.redisConnectionFactory == null) {
                this.redisDetail.set("RedisConnectionFactory 빈을 찾을 수 없습니다.");
            }
            else {
                RedisConnection connection = this.redisConnectionFactory.getConnection();
                try {
                    String pong = connection.ping();
                    available = "PONG".equalsIgnoreCase(pong);
                    this.redisDetail.set("PING 프로브가 PONG 응답을 반환했습니다.");
                }
                finally {
                    connection.close();
                }
            }
        }
        catch (Exception exception) {
            this.redisDetail.set(compactMessage(exception));
            this.redisFailures.increment();
        }

        long elapsed = elapsedMillis(startedAt);
        this.redisAvailability.set(available ? 1 : 0);
        this.redisLatencyMs.set((int) elapsed);
        this.redisCheckedAt.set(Instant.now());
        this.redisProbeTimer.record(Duration.ofMillis(elapsed));
    }

    private static long elapsedMillis(Instant startedAt) {
        return Math.max(1L, Duration.between(startedAt, Instant.now()).toMillis());
    }

    private static String compactMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        String compact = message.replaceAll("\\s+", " ").trim();
        if (compact.length() <= 90) {
            return exception.getClass().getSimpleName() + ": " + compact;
        }

        return exception.getClass().getSimpleName() + ": " + compact.substring(0, 87) + "...";
    }
}
