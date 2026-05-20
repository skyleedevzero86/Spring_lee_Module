package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sleekydz86.monitoring.logstack_s3.application.assembler.StorageViewAssembler;
import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.application.query.BrowseStorageQuery;
import com.sleekydz86.monitoring.logstack_s3.domain.model.ListedStorageObject;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StorageBucket;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.StorageBucketRepository;
import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrowseStorageUseCase 테스트")
class BrowseStorageUseCaseTest {

    @Mock
    private StorageBucketRepository storageBucketRepository;

    @Mock
    private ObjectStoragePort objectStorage;

    private BrowseStorageUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BrowseStorageUseCase(
                storageBucketRepository,
                objectStorage,
                new StorageViewAssembler(objectStorage));
    }

    @Test
    @DisplayName("성공 - 키워드·prefix 필터 및 페이징")
    void apply_filterAndPaging_success() {
        // given
        Instant older = Instant.parse("2026-05-20T10:00:00Z");
        Instant newer = Instant.parse("2026-05-20T11:00:00Z");
        given(storageBucketRepository.findByBucketCode("erp-bucket"))
                .willReturn(Optional.of(new StorageBucket(1L, "erp-bucket", "us-east-1", "ERP", LocalDateTime.now())));
        given(objectStorage.listObjects("erp-bucket")).willReturn(List.of(
                new ListedStorageObject("uploads/a_old.png", 100L, older),
                new ListedStorageObject("uploads/b_new.png", 200L, newer),
                new ListedStorageObject("thumbnails/t.jpg", 50L, newer),
                new ListedStorageObject("test.txt", 10L, newer)
        ));
        given(objectStorage.presignPreview("erp-bucket", "uploads/b_new.png")).willReturn("https://preview-b");

        // when
        var result = useCase.apply(new BrowseStorageQuery("erp-bucket", "b_new", StorageObjectPaths.PREFIX_UPLOADS, 0, 10));

        // then
        assertThat(result.bucketCode()).isEqualTo("erp-bucket");
        assertThat(result.page().totalElements()).isEqualTo(1);
        assertThat(result.page().content()).hasSize(1);
        assertThat(result.page().content().getFirst().key()).isEqualTo("uploads/b_new.png");
    }

    @Test
    @DisplayName("성공 - thumbnails prefix 페이징")
    void apply_thumbnailsPrefix_success() {
        // given
        given(storageBucketRepository.findByBucketCode("erp-bucket"))
                .willReturn(Optional.of(new StorageBucket(1L, "erp-bucket", "us-east-1", "ERP", LocalDateTime.now())));
        given(objectStorage.listObjects("erp-bucket")).willReturn(List.of(
                new ListedStorageObject("thumbnails/one.jpg", 50L, Instant.now()),
                new ListedStorageObject("uploads/two.png", 100L, Instant.now())
        ));
        given(objectStorage.presignPreview("erp-bucket", "thumbnails/one.jpg")).willReturn("https://thumb");
        given(objectStorage.findFirstObjectKey("erp-bucket", "uploads/one_")).willReturn(Optional.of("uploads/one_file.png"));
        given(objectStorage.presignPreview("erp-bucket", "uploads/one_file.png")).willReturn("https://original");

        // when
        var result = useCase.apply(new BrowseStorageQuery("erp-bucket", null, StorageObjectPaths.PREFIX_THUMBNAILS, 0, 12));

        // then
        assertThat(result.page().totalElements()).isEqualTo(1);
        assertThat(result.page().content().getFirst().originalPreviewUrl()).isEqualTo("https://original");
    }
}
