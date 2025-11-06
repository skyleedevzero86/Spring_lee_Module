package com.sleekydz86.ocrstudy1.infrastructure.adapter.in.thymeleaf;

import com.sleekydz86.ocrstudy1.application.port.in.ImageCompareUseCase;
import com.sleekydz86.ocrstudy1.application.port.in.ImageUploadUseCase;
import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import com.sleekydz86.ocrstudy1.doamin.model.ImageComparison;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import com.sleekydz86.ocrstudy1.doamin.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageWebController 테스트")
class ImageWebControllerTest {

    @Mock
    private ImageUploadUseCase imageUploadUseCase;

    @Mock
    private ImageCompareUseCase imageCompareUseCase;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ImageWebController controller;

    private Image testImage;

    @BeforeEach
    void setUp() {
        testImage = Image.builder()
                .id(1L)
                .originalFilename("test.jpg")
                .storedFilename("test.jpg")
                .filePath("test.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .minioObjectName("test.jpg")
                .documentType(DocumentType.RECEIPT)
                .encryptedOcrText("encrypted_text")
                .encryptedExtractedIdInfo("encrypted_id_info")
                .build();
    }

    @Test
    @DisplayName("인덱스 페이지 조회 - 전체 이미지")
    void index_AllImages() {
        List<Image> images = List.of(testImage);
        Page<Image> imagePage = new PageImpl<>(images, PageRequest.of(0, 10), 1);

        when(imageRepository.findAll(any(Pageable.class))).thenReturn(imagePage);

        String viewName = controller.index(0, 10, null, model);

        assertThat(viewName).isEqualTo("index");
        verify(model).addAttribute("images", images);
        verify(model).addAttribute("currentPage", 0);
        verify(model).addAttribute("totalPages", 1);
    }

    @Test
    @DisplayName("인덱스 페이지 조회 - 문서 타입 필터")
    void index_WithDocumentTypeFilter() {
        List<Image> images = List.of(testImage);
        Page<Image> imagePage = new PageImpl<>(images, PageRequest.of(0, 10), 1);

        when(imageRepository.findByDocumentType(eq(DocumentType.RECEIPT), any(Pageable.class)))
                .thenReturn(imagePage);

        String viewName = controller.index(0, 10, DocumentType.RECEIPT, model);

        assertThat(viewName).isEqualTo("index");
        verify(imageRepository).findByDocumentType(DocumentType.RECEIPT, any(Pageable.class));
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImage_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        ImageUploadUseCase.UploadResult result = new ImageUploadUseCase.UploadResult(
                1L, "test.jpg", "OCR 텍스트", false, false, null
        );

        when(imageUploadUseCase.uploadAndProcess(any(MultipartFile.class))).thenReturn(result);

        String viewName = controller.uploadImage(file, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/");
        verify(redirectAttributes).addFlashAttribute("success", "파일 업로드 및 처리 완료");
        verify(redirectAttributes).addFlashAttribute("result", result);
    }

    @Test
    @DisplayName("이미지 업로드 실패 - 빈 파일")
    void uploadImage_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        String viewName = controller.uploadImage(emptyFile, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/");
        verify(redirectAttributes).addFlashAttribute("error", "파일이 비어있습니다.");
        verify(imageUploadUseCase, never()).uploadAndProcess(any());
    }

    @Test
    @DisplayName("이미지 상세 조회 - 복호화 포함")
    void viewImage_Success_WithDecryption() {
        ImageComparison comparison = ImageComparison.builder()
                .currentImageId(1L)
                .similarityScore(0.75)
                .comparisonType(ImageComparison.ComparisonType.FACE_SIMILARITY)
                .details("Comparison details")
                .build();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));
        when(encryptionService.decrypt("encrypted_text")).thenReturn("복호화된 OCR 텍스트");
        when(encryptionService.decrypt("encrypted_id_info")).thenReturn("복호화된 신분증 정보");
        when(imageCompareUseCase.compareWithLatest(1L)).thenReturn(comparison);

        String viewName = controller.viewImage(1L, model);

        assertThat(viewName).isEqualTo("image-detail");
        verify(encryptionService).decrypt("encrypted_text");
        verify(encryptionService).decrypt("encrypted_id_info");
        verify(model).addAttribute("image", testImage);
        verify(model).addAttribute("comparison", comparison);
    }

    @Test
    @DisplayName("이미지 상세 조회 - 이미지 없음")
    void viewImage_NotFound() {
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        String viewName = controller.viewImage(1L, model);

        assertThat(viewName).isEqualTo("image-detail");
        verify(encryptionService, never()).decrypt(anyString());
    }

    @Test
    @DisplayName("이미지 상세 조회 - 복호화 실패 시 평문 사용")
    void viewImage_DecryptionFailure_UsesPlainText() {
        testImage.setOcrText("평문 텍스트");
        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));
        when(encryptionService.decrypt("encrypted_text"))
                .thenThrow(new RuntimeException("Decryption failed"));
        when(imageCompareUseCase.compareWithLatest(1L))
                .thenReturn(ImageComparison.builder().build());

        String viewName = controller.viewImage(1L, model);

        assertThat(viewName).isEqualTo("image-detail");
        assertThat(testImage.getOcrText()).isEqualTo("평문 텍스트");
    }
}
