package com.sleekydz86.monitoring.logstack_s3.interfaces.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.sleekydz86.monitoring.logstack_s3.application.query.SearchFilesQuery;
import com.sleekydz86.monitoring.logstack_s3.application.query.UploadFileCommand;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.DeleteFileUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.GetFileDetailUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.SearchFilesUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.usecase.UploadFileUseCase;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileDetailView;
import com.sleekydz86.monitoring.logstack_s3.application.view.FileListItemView;
import com.sleekydz86.monitoring.logstack_s3.global.common.message.KoreanMessages;
import com.sleekydz86.monitoring.logstack_s3.domain.exception.FileNotFoundException;
import com.sleekydz86.monitoring.logstack_s3.domain.model.PageResult;
import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileApiController API 테스트")
class FileApiControllerTest {

    private MockMvc mockMvc;
    private UploadFileUseCase uploadFileUseCase;
    private SearchFilesUseCase searchFilesUseCase;
    private GetFileDetailUseCase getFileDetailUseCase;
    private DeleteFileUseCase deleteFileUseCase;

    @BeforeEach
    void setUp() {
        // given
        uploadFileUseCase = mock(UploadFileUseCase.class);
        searchFilesUseCase = mock(SearchFilesUseCase.class);
        getFileDetailUseCase = mock(GetFileDetailUseCase.class);
        deleteFileUseCase = mock(DeleteFileUseCase.class);
        FileApiController controller = new FileApiController(
                uploadFileUseCase, searchFilesUseCase, getFileDetailUseCase, deleteFileUseCase
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("성공 - 업로드 API")
    void upload_success() throws Exception {
        // given
        var file = TestFileFixtures.storedFile();
        var view = new FileDetailView(
                file.id(), file.originalFilename(), file.contentType(), file.size(),
                file.createdAt(), file.objectKey(), "t", "p", "d", true, false
        );
        given(uploadFileUseCase.apply(any(UploadFileCommand.class))).willReturn(view);

        // when & then
        mockMvc.perform(multipart("/api/files/upload")
                        .file(new MockMultipartFile("file", "a.png", "image/png", new byte[]{1})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(file.id()));
    }

    @Test
    @DisplayName("성공 - 목록 API")
    void list_success() throws Exception {
        // given
        var item = new FileListItemView(
                "lky_20260520_1430_0001", "a.png", "image/png", 1L,
                TestFileFixtures.FIXED_TIME, null, "bucket", "us-east-1", "1kB", "IMAGE"
        );
        given(searchFilesUseCase.apply(any(SearchFilesQuery.class)))
                .willReturn(PageResult.of(java.util.List.of(item), 0, 12, 1));

        // when & then
        mockMvc.perform(get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].originalFilename").value("a.png"));
    }

    @Test
    @DisplayName("실패 - 상세 API 파일 없음")
    void detail_notFound_fail() throws Exception {
        // given
        String id = "lky_20260520_1430_9999";
        given(getFileDetailUseCase.apply(id)).willThrow(new FileNotFoundException(id));

        // when & then
        mockMvc.perform(get("/api/files/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(KoreanMessages.fileNotFound(id)));
    }

    @Test
    @DisplayName("성공 - 삭제 API")
    void delete_success() throws Exception {
        // given
        String id = "lky_20260520_1430_0001";

        // when
        mockMvc.perform(delete("/api/files/{id}", id))
                .andExpect(status().isNoContent());

        // then
        verify(deleteFileUseCase).apply(id);
    }
}
