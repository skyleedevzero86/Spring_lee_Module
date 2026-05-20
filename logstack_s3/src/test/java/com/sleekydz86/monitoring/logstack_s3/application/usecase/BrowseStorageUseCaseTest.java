package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;

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
import com.sleekydz86.monitoring.logstack_s3.domain.service.StorageObjectPaths;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrowseStorageUseCase 테스트")
class BrowseStorageUseCaseTest {

    @Mock
    private ObjectStoragePort objectStorage;

    private BrowseStorageUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BrowseStorageUseCase(objectStorage, new StorageViewAssembler(objectStorage));
    }

    @Test
    @DisplayName("성공 - 키워드·prefix 필터 후 최신순 정렬")
    void apply_filterAndSort_success() {
        // given
        Instant older = Instant.parse("2026-05-20T10:00:00Z");
        Instant newer = Instant.parse("2026-05-20T11:00:00Z");
        given(objectStorage.bucketName()).willReturn("erp-bucket");
        given(objectStorage.listObjects()).willReturn(List.of(
                new ListedStorageObject("uploads/a_old.png", 100L, older),
                new ListedStorageObject("uploads/b_new.png", 200L, newer),
                new ListedStorageObject("thumbnails/t.jpg", 50L, newer),
                new ListedStorageObject("test.txt", 10L, newer)
        ));
        given(objectStorage.presignPreview("uploads/b_new.png")).willReturn("https://preview-b");

        // when
        var result = useCase.apply(new BrowseStorageQuery("b_new", StorageObjectPaths.PREFIX_UPLOADS));

        // then
        assertThat(result.bucketName()).isEqualTo("erp-bucket");
        assertThat(result.objectCount()).isEqualTo(1);
        assertThat(result.objects()).hasSize(1);
        assertThat(result.objects().getFirst().key()).isEqualTo("uploads/b_new.png");
        assertThat(result.objects().getFirst().previewUrl()).isEqualTo("https://preview-b");
    }

    @Test
    @DisplayName("성공 - thumbnails prefix만 조회")
    void apply_thumbnailsPrefix_success() {
        // given
        given(objectStorage.bucketName()).willReturn("erp-bucket");
        given(objectStorage.listObjects()).willReturn(List.of(
                new ListedStorageObject("thumbnails/one.jpg", 50L, Instant.now()),
                new ListedStorageObject("uploads/two.png", 100L, Instant.now())
        ));
        given(objectStorage.presignPreview("thumbnails/one.jpg")).willReturn("https://thumb");

        // when
        var result = useCase.apply(new BrowseStorageQuery(null, StorageObjectPaths.PREFIX_THUMBNAILS));

        // then
        assertThat(result.objectCount()).isEqualTo(1);
        assertThat(result.objects().getFirst().kindLabel()).isEqualTo("썸네일");
    }
}
