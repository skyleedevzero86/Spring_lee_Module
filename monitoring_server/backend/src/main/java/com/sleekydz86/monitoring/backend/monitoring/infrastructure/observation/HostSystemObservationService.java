package com.sleekydz86.monitoring.backend.monitoring.infrastructure.observation;


import com.sun.management.OperatingSystemMXBean;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class HostSystemObservationService {

    public record HostSnapshot(
            String hostName,
            String operatingSystemFamily,
            String operatingSystem,
            String architecture,
            String javaRuntime,
            int availableProcessors,
            long totalMemoryBytes,
            long usedMemoryBytes,
            long freeMemoryBytes,
            double memoryUsagePercent,
            long totalDiskBytes,
            long usedDiskBytes,
            long freeDiskBytes,
            double diskUsagePercent,
            String diskPath,
            Instant capturedAt
    ) {
    }

    private final AtomicReference<String> hostName = new AtomicReference<>("unknown");
    private final AtomicReference<String> operatingSystemFamily = new AtomicReference<>("OTHER");
    private final AtomicReference<String> operatingSystem = new AtomicReference<>("unknown");
    private final AtomicReference<String> architecture = new AtomicReference<>("unknown");
    private final AtomicReference<String> javaRuntime = new AtomicReference<>("unknown");
    private final AtomicReference<String> diskPath = new AtomicReference<>(".");
    private final AtomicReference<Instant> capturedAt = new AtomicReference<>(Instant.EPOCH);

    private final AtomicInteger availableProcessors = new AtomicInteger();
    private final AtomicLong totalMemoryBytes = new AtomicLong();
    private final AtomicLong usedMemoryBytes = new AtomicLong();
    private final AtomicLong freeMemoryBytes = new AtomicLong();
    private final AtomicLong totalDiskBytes = new AtomicLong();
    private final AtomicLong usedDiskBytes = new AtomicLong();
    private final AtomicLong freeDiskBytes = new AtomicLong();

    public HostSystemObservationService(MeterRegistry meterRegistry) {
        Gauge.builder("monitoring.host.cpu.count", this.availableProcessors, AtomicInteger::get)
                .description("Available processors of the host machine")
                .register(meterRegistry);
        Gauge.builder("monitoring.host.memory.total", this.totalMemoryBytes, AtomicLong::get)
                .description("Total physical memory of the host")
                .baseUnit("bytes")
                .register(meterRegistry);
        Gauge.builder("monitoring.host.memory.used", this.usedMemoryBytes, AtomicLong::get)
                .description("Used physical memory of the host")
                .baseUnit("bytes")
                .register(meterRegistry);
        Gauge.builder("monitoring.host.memory.free", this.freeMemoryBytes, AtomicLong::get)
                .description("Free physical memory of the host")
                .baseUnit("bytes")
                .register(meterRegistry);
        Gauge.builder("monitoring.host.disk.total", this.totalDiskBytes, AtomicLong::get)
                .description("Total disk size of the host filesystem that runs the app")
                .baseUnit("bytes")
                .register(meterRegistry);
        Gauge.builder("monitoring.host.disk.used", this.usedDiskBytes, AtomicLong::get)
                .description("Used disk size of the host filesystem that runs the app")
                .baseUnit("bytes")
                .register(meterRegistry);
        Gauge.builder("monitoring.host.disk.free", this.freeDiskBytes, AtomicLong::get)
                .description("Free disk size of the host filesystem that runs the app")
                .baseUnit("bytes")
                .register(meterRegistry);
        Gauge.builder("monitoring.host.memory.usage", this::memoryUsagePercent)
                .description("Physical memory usage percent of the host")
                .register(meterRegistry);
        Gauge.builder("monitoring.host.disk.usage", this::diskUsagePercent)
                .description("Disk usage percent of the host filesystem that runs the app")
                .register(meterRegistry);

        refresh();
    }

    @Scheduled(fixedDelayString = "${idolglow.monitoring.host-sample-interval-ms:15000}", initialDelay = 2000)
    public void refresh() {
        this.hostName.set(resolveHostName());

        String osName = System.getProperty("os.name", "unknown");
        String osVersion = System.getProperty("os.version", "");
        this.operatingSystemFamily.set(resolveOperatingSystemFamily(osName));
        this.operatingSystem.set(osVersion.isBlank() ? osName : osName + " " + osVersion);
        this.architecture.set(System.getProperty("os.arch", "unknown"));
        this.javaRuntime.set(System.getProperty("java.vm.name", "unknown") + " / Java " + Runtime.version().feature());
        this.availableProcessors.set(Runtime.getRuntime().availableProcessors());

        OperatingSystemMXBean operatingSystemMxBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMemory = Math.max(0L, operatingSystemMxBean.getTotalMemorySize());
        long freeMemory = Math.max(0L, operatingSystemMxBean.getFreeMemorySize());
        this.totalMemoryBytes.set(totalMemory);
        this.freeMemoryBytes.set(freeMemory);
        this.usedMemoryBytes.set(Math.max(0L, totalMemory - freeMemory));

        captureDiskUsage();
        this.capturedAt.set(Instant.now());
    }

    public HostSnapshot snapshot() {
        return new HostSnapshot(
                this.hostName.get(),
                this.operatingSystemFamily.get(),
                this.operatingSystem.get(),
                this.architecture.get(),
                this.javaRuntime.get(),
                this.availableProcessors.get(),
                this.totalMemoryBytes.get(),
                this.usedMemoryBytes.get(),
                this.freeMemoryBytes.get(),
                memoryUsagePercent(),
                this.totalDiskBytes.get(),
                this.usedDiskBytes.get(),
                this.freeDiskBytes.get(),
                diskUsagePercent(),
                this.diskPath.get(),
                this.capturedAt.get()
        );
    }

    private void captureDiskUsage() {
        try {
            Path workingPath = Path.of("").toAbsolutePath();
            FileStore fileStore = Files.getFileStore(workingPath);
            long total = Math.max(0L, fileStore.getTotalSpace());
            long free = Math.max(0L, fileStore.getUsableSpace());
            this.diskPath.set(workingPath.getRoot() == null ? workingPath.toString() : workingPath.getRoot().toString());
            this.totalDiskBytes.set(total);
            this.freeDiskBytes.set(free);
            this.usedDiskBytes.set(Math.max(0L, total - free));
        }
        catch (Exception ignored) {
            this.diskPath.set(Path.of("").toAbsolutePath().toString());
            this.totalDiskBytes.set(0L);
            this.freeDiskBytes.set(0L);
            this.usedDiskBytes.set(0L);
        }
    }

    private double memoryUsagePercent() {
        long total = this.totalMemoryBytes.get();
        if (total <= 0L) {
            return 0.0;
        }

        return (this.usedMemoryBytes.get() * 100.0) / total;
    }

    private double diskUsagePercent() {
        long total = this.totalDiskBytes.get();
        if (total <= 0L) {
            return 0.0;
        }

        return (this.usedDiskBytes.get() * 100.0) / total;
    }

    private static String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (Exception ignored) {
            return "unknown-host";
        }
    }

    private static String resolveOperatingSystemFamily(String osName) {
        String normalized = osName.toLowerCase();
        if (normalized.contains("win")) {
            return "WINDOWS";
        }
        if (normalized.contains("linux")) {
            return "LINUX";
        }
        if (normalized.contains("mac")) {
            return "MAC";
        }
        return "OTHER";
    }
}
