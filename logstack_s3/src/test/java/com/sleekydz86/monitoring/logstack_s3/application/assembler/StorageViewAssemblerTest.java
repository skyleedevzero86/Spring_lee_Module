package com.sleekydz86.monitoring.logstack_s3.application.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;

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
    @DisplayName("성공 - 이미지 객체는 presigned URL 포함")
    void toView_image_success() {
        // given
        var object = new ListedStorageObject(
                "uploads/uuid_photo.png",
                2048L,
                Instant.parse("2026-05-20T12:00:00Z"));
        given(objectStorage.presignPreview(object.key())).willReturn("https://signed");

        // when
        var view = assembler.toView(object);

        // then
        assertThat(view.displayName()).isEqualTo("photo.png");
        assertThat(view.kindLabel()).isEqualTo("원본");
        assertThat(view.sizeLabel()).isEqualTo("2 kB");
        assertThat(view.previewUrl()).isEqualTo("https://signed");
        assertThat(view.image()).isTrue();
    }

    @Test
    @DisplayName("성공 - 비이미지 객체는 미리보기 URL 없음")
    void toView_nonImage_success() {
        // given
        var object = new ListedStorageObject("test.txt", 40L, Instant.now());

        // when
        var view = assembler.toView(object);

        // then
        assertThat(view.previewUrl()).isNull();
        assertThat(view.image()).isFalse();
        assertThat(view.kindLabel()).isEqualTo("기타");
    }
}
