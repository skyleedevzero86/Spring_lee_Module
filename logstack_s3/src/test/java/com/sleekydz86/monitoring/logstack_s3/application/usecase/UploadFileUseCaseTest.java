package com.sleekydz86.monitoring.logstack_s3.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.sleekydz86.monitoring.logstack_s3.application.assembler.FileViewAssembler;
import com.sleekydz86.monitoring.logstack_s3.application.port.ObjectStoragePort;
import com.sleekydz86.monitoring.logstack_s3.application.port.ThumbnailPort;
import com.sleekydz86.monitoring.logstack_s3.application.query.UploadFileCommand;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileDetailView;
import com.sleekydz86.monitoring.logstack_s3.domain.message.DomainMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileStorageException;
import com.sleekydz86.monitoring.logstack_s3.domain.model.StoredFile;
import com.sleekydz86.monitoring.logstack_s3.domain.repository.FileRepository;
import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadFileUseCase 테스트")
class UploadFileUseCaseTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private ObjectStoragePort objectStorage;

    @Mock
    private ThumbnailPort thumbnailPort;

    @Mock
    private FileViewAssembler assembler;

    @InjectMocks
    private UploadFileUseCase useCase;

    @Test
    @DisplayName("성공 - 파일 업로드")
    void apply_success() throws IOException {
        // given
        var multipart = TestFileFixtures.imageMultipartFile();
        var saved = TestFileFixtures.storedFile();
        var view = new FileDetailView(
                saved.id(), saved.originalFilename(), saved.contentType(), saved.size(),
                saved.createdAt(), saved.objectKey(), "t", "p", "d", true, false);
        given(thumbnailPort.generate(any(), anyString())).willReturn(Optional.of(new byte[] { 1, 2 }));
        given(thumbnailPort.contentType()).willReturn("image/jpeg");
        given(fileRepository.save(any(StoredFile.class))).willReturn(saved);
        given(assembler.toDetail(saved)).willReturn(view);

        // when
        FileDetailView result = useCase.apply(new UploadFileCommand(multipart));

        // then
        assertThat(result.id()).isEqualTo(saved.id());
        verify(objectStorage).put(anyString(), anyString(), anyLong(), any(InputStream.class));
        verify(fileRepository).save(any(StoredFile.class));
    }

    @Test
    @DisplayName("실패 - 파일 읽기 오류")
    void apply_readFail_fail() throws IOException {
        // given
        MultipartFile broken = org.mockito.Mockito.mock(MultipartFile.class);
        given(broken.getOriginalFilename()).willReturn("broken.png");
        given(broken.getContentType()).willReturn("image/png");
        given(broken.getSize()).willReturn(10L);
        given(broken.getInputStream()).willThrow(new IOException("disk error"));

        // when & then
        assertThatThrownBy(() -> useCase.apply(new UploadFileCommand(broken)))
                .isInstanceOf(FileStorageException.class)
                .hasMessage(com.sleekydz86.monitoring.logstack_s3.infrastructure.message.InfrastructureMessages.FILE_READ_FAILED);
    }
}
