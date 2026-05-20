package com.sleekydz86.monitoring.logstack_s3.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFile;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;
import com.sleekydz86.monitoring.logstack_s3.support.IntegrationTestBase;
import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@Tag("integration")
@DisplayName("파일 ID 동시성 통합 테스트")
class FileIdConcurrencyIntegrationTest extends IntegrationTestBase {

    @Autowired
    private FileRepository fileRepository;

    @Test
    @DisplayName("성공 - 동시 저장 시 ID 중복 없음")
    void concurrentSave_uniqueIds() throws Exception {
        // given
        LocalDateTime fixedMinute = TestFileFixtures.FIXED_TIME;
        int threadCount = 8;
        var executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<String>> tasks = new ArrayList<>();

        IntStream.range(0, threadCount).forEach(i -> tasks.add(() -> {
            StoredFile draft = new StoredFile(
                    null,
                    "concurrent-" + i + ".png",
                    "uploads/concurrent-" + i + ".png",
                    "thumbnails/concurrent-" + i + ".jpg",
                    "image/png",
                    100L + i,
                    fixedMinute);
            return fileRepository.save(draft).id();
        }));

        // when
        var futures = executor.invokeAll(tasks);
        executor.shutdown();

        List<String> ids = new ArrayList<>();
        for (var future : futures) {
            ids.add(future.get());
        }

        // then
        assertThat(ids).hasSize(threadCount);
        assertThat(Set.copyOf(ids)).hasSize(threadCount);
        ids.forEach(id -> assertThat(id).startsWith("it_"));
    }
}
