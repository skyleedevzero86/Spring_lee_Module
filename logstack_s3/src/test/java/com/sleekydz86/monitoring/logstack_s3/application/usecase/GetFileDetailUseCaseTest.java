package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sleekydz86.monitoring.logstack_s3.application.assembler.FileViewAssembler;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileDetailView;
import com.sleekydz86.monitoring.logstack_s3.global.common.message.KoreanMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileNotFoundException;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;
import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetFileDetailUseCase 테스트")
class GetFileDetailUseCaseTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileViewAssembler assembler;

    @InjectMocks
    private GetFileDetailUseCase useCase;

    @Test
    @DisplayName("성공 - 파일 상세 조회")
    void apply_success() {
        // given
        var file = TestFileFixtures.storedFile();
        var view = new FileDetailView(
                file.id(), file.originalFilename(), file.contentType(), file.size(),
                file.createdAt(), file.objectKey(), "thumb-url", "preview-url", "download-url",
                true, false
        );
        given(fileRepository.findById(file.id())).willReturn(Optional.of(file));
        given(assembler.toDetail(file)).willReturn(view);

        // when
        FileDetailView result = useCase.apply(file.id());

        // then
        assertThat(result.id()).isEqualTo("lky_20260520_1430_0001");
        assertThat(result.previewUrl()).isEqualTo("preview-url");
    }

    @Test
    @DisplayName("실패 - 파일 없음")
    void apply_notFound_fail() {
        // given
        String id = "lky_20260520_1430_9999";
        given(fileRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> useCase.apply(id))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage(KoreanMessages.fileNotFound(id));
    }
}
