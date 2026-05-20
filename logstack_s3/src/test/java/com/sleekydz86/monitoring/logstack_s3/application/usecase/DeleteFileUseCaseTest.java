package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileNotFoundException;
import com.sleekydz86.monitoring.logstack_s3.domain.message.DomainMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;
import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteFileUseCase 테스트")
class DeleteFileUseCaseTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ObjectStoragePort objectStorage;

    private DeleteFileUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteFileUseCase(fileRepository, objectStorage);
    }

    @Test
    @DisplayName("성공 - 파일 삭제 및 S3 객체 정리")
    void apply_success() {
        // given
        var file = TestFileFixtures.storedFile();
        given(fileRepository.findById(file.id())).willReturn(Optional.of(file));

        // when
        assertThatCode(() -> useCase.apply(file.id())).doesNotThrowAnyException();

        // then
        verify(objectStorage).delete(file.objectKey());
        verify(objectStorage).delete(file.thumbnailKey());
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
                .hasMessage(DomainMessages.fileNotFound(id));
    }
}
