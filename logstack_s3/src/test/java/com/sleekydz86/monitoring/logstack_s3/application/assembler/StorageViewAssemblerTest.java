package com.sleekydz86.monitoring.logstack_s3.application.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.domain.model.ListedStorageObject;

@ExtendWith(MockitoExtension.class)
@DisplayName("StorageViewAssembler 테스트")
class StorageViewAssemblerTest {

    @Mock
    private ObjectStoragePort objectStorage;

    @InjectMocks
    private StorageViewAssembler assembler;

    @Test
    @DisplayName("성공 - uploads 이미지는 동일 URL")
    void toView_uploadImage_success() {
        // given
        var object = new ListedStorageObject(
                "uploads/uuid_photo.png",
                2048L,
                Instant.parse("2026-05-20T12:00:00Z"));
        given(objectStorage.presignPreview("erp-bucket", object.key())).willReturn("https://signed");

        // when
        var view = assembler.toView("erp-bucket", object);

        // then
        assertThat(view.displayName()).isEqualTo("photo.png");
        assertThat(view.previewUrl()).isEqualTo("https://signed");
        assertThat(view.originalPreviewUrl()).isEqualTo("https://signed");
        assertThat(view.thumbnail()).isFalse();
    }

    @Test
    @DisplayName("성공 - 썸네일은 원본 presigned URL 연결")
    void toView_thumbnail_success() {
        // given
        var object = new ListedStorageObject("thumbnails/uuid.jpg", 50L, Instant.now());
        given(objectStorage.presignPreview("erp-bucket", object.key())).willReturn("https://thumb");
        given(objectStorage.findFirstObjectKey("erp-bucket", "uploads/uuid_"))
                .willReturn(Optional.of("uploads/uuid_file.png"));
        given(objectStorage.presignPreview("erp-bucket", "uploads/uuid_file.png")).willReturn("https://original");

        // when
        var view = assembler.toView("erp-bucket", object);

        // then
        assertThat(view.thumbnail()).isTrue();
        assertThat(view.previewUrl()).isEqualTo("https://thumb");
        assertThat(view.originalPreviewUrl()).isEqualTo("https://original");
    }
}
