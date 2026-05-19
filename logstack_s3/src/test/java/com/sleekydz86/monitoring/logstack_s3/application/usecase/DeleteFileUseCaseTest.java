package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sleekydz86.monitoring.logstack_s3.global.common.message.KoreanMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileNotFoundException;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;
import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteFileUseCase 테스트")
class DeleteFileUseCaseTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private DeleteFileUseCase useCase;

    @Test
    @DisplayName("성공 - 파일 삭제")
    void apply_success() {
        // given
        var file = TestFileFixtures.storedFile();
        given(fileRepository.findById(file.id())).willReturn(Optional.of(file));

        // when
        assertThatCode(() -> useCase.apply(file.id())).doesNotThrowAnyException();

        // then
        verify(fileRepository).delete(file.id());
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
