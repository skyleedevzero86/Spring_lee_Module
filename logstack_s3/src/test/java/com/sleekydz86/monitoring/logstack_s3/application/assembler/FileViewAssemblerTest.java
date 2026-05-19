package com.sleekydz86.monitoring.logstack_s3.application.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileDetailView;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileListItemView;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFileSummary;
import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileViewAssembler 테스트")
class FileViewAssemblerTest {

    @Mock
    private ObjectStoragePort objectStorage;

    @InjectMocks
    private FileViewAssembler assembler;

    @Test
    @DisplayName("성공 - 목록 View 변환")
    void toListItem_success() {
        // given
        var summary = TestFileFixtures.storedFileSummary();
        given(objectStorage.presignPreview(summary.thumbnailKey())).willReturn("https://thumb");

        // when
        FileListItemView view = assembler.toListItem(summary);

        // then
        assertThat(view.thumbnailUrl()).isEqualTo("https://thumb");
        assertThat(view.mediaType()).isEqualTo("IMAGE");
    }

    @Test
    @DisplayName("성공 - 상세 View 변환")
    void toDetail_success() {
        // given
        var file = TestFileFixtures.storedFile();
        given(objectStorage.presignPreview(file.thumbnailKey())).willReturn("https://thumb");
        given(objectStorage.presignPreview(file.objectKey())).willReturn("https://preview");
        given(objectStorage.presignDownload(file.objectKey(), file.originalFilename()))
                .willReturn("https://download");

        // when
        FileDetailView view = assembler.toDetail(file);

        // then
        assertThat(view.image()).isTrue();
        assertThat(view.previewUrl()).isEqualTo("https://preview");
        assertThat(view.downloadUrl()).isEqualTo("https://download");
    }

    @Test
    @DisplayName("성공 - 썸네일 없으면 URL null")
    void toListItem_noThumbnail_success() {
        // given
        StoredFileSummary summary = new StoredFileSummary(
                "id", "f", "k", null, "text/plain", 1L, TestFileFixtures.FIXED_TIME,
                "b", "r", "d", "1B", "FILE"
        );

        // when
        FileListItemView view = assembler.toListItem(summary);

        // then
        assertThat(view.thumbnailUrl()).isNull();
    }
}
