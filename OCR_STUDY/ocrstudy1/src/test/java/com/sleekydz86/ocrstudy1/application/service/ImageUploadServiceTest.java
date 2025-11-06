package com.sleekydz86.ocrstudy1.application.service;

import com.sleekydz86.ocrstudy1.application.port.in.IdVerificationUseCase;
import com.sleekydz86.ocrstudy1.application.port.in.ImageUploadUseCase;
import com.sleekydz86.ocrstudy1.application.port.out.DocumentTypeDetectionPort;
import com.sleekydz86.ocrstudy1.application.port.out.FaceRecognitionPort;
import com.sleekydz86.ocrstudy1.application.port.out.OcrServicePort;
import com.sleekydz86.ocrstudy1.application.port.out.StoragePort;
import com.sleekydz86.ocrstudy1.doamin.model.*;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import com.sleekydz86.ocrstudy1.doamin.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageUploadService 테스트 - 비동기 처리 포함")
class ImageUploadServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private StoragePort storagePort;

    @Mock
    private OcrServicePort ocrServicePort;

    @Mock
    private FaceRecognitionPort faceRecognitionPort;

    @Mock
    private IdVerificationUseCase idVerificationUseCase;

    @Mock
    private DocumentTypeDetectionPort documentTypeDetectionPort;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private ImageUploadService imageUploadService;

    private MultipartFile mockFile;
    private Image savedImage;
    private OcrResult ocrResult;
    private FaceRecognition faceRecognition;

    @BeforeEach
    void setUp() {
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024L);

        savedImage = Image.builder()
                .id(1L)
                .originalFilename("test.jpg")
                .storedFilename("test.jpg")
                .filePath("test.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .minioObjectName("1234567890_test.jpg")
                .build();

        ocrResult = OcrResult.builder()
                .text("테스트 OCR 텍스트")
                .confidence(0.95)
                .language("kor+eng")
                .boundingBoxes(new ArrayList<>())
                .build();

        faceRecognition = FaceRecognition.builder()
                .hasFace(false)
                .faceCount(0)
                .faces(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("이미지 업로드 및 처리 성공 - OCR 텍스트 추출")
    void uploadAndProcess_Success_WithOcrText() throws IOException {
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(storagePort.uploadFile(any(InputStream.class), anyString(), anyString(), anyLong()))
                .thenReturn("1234567890_test.jpg");
        when(storagePort.downloadFile(anyString()))
                .thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
        when(ocrServicePort.extractText(any(InputStream.class), anyString())).thenReturn(ocrResult);
        when(documentTypeDetectionPort.detectDocumentType(any(InputStream.class), anyString(), anyString()))
                .thenReturn(DocumentType.ETC);
        when(faceRecognitionPort.detectFaces(any(InputStream.class), anyString())).thenReturn(faceRecognition);
        when(idVerificationUseCase.verifyIdCard(anyLong())).thenReturn(
                IdVerification.builder().isIdCard(false).build());
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted_text");

        ImageUploadUseCase.UploadResult result = imageUploadService.uploadAndProcess(mockFile);

        assertThat(result).isNotNull();
        assertThat(result.imageId()).isEqualTo(1L);
        assertThat(result.filename()).isEqualTo("test.jpg");
        assertThat(result.ocrText()).isEqualTo("테스트 OCR 텍스트");
        assertThat(result.hasFace()).isFalse();
        assertThat(result.isIdCard()).isFalse();

        verify(storagePort).uploadFile(any(), anyString(), anyString(), anyLong());
        verify(ocrServicePort).extractText(any(), anyString());
        verify(encryptionService).encrypt("테스트 OCR 텍스트");
        verify(imageRepository, times(2)).save(any(Image.class));
    }

    @Test
    @DisplayName("이미지 업로드 및 처리 성공 - 얼굴 감지")
    void uploadAndProcess_Success_WithFaceDetection() throws IOException {
        FaceRecognition faceRecognitionWithFace = FaceRecognition.builder()
                .hasFace(true)
                .faceCount(1)
                .faces(new ArrayList<>())
                .build();

        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(storagePort.uploadFile(any(), anyString(), anyString(), anyLong()))
                .thenReturn("1234567890_test.jpg");
        when(storagePort.downloadFile(anyString()))
                .thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
        when(ocrServicePort.extractText(any(), anyString())).thenReturn(ocrResult);
        when(documentTypeDetectionPort.detectDocumentType(any(), anyString(), anyString()))
                .thenReturn(DocumentType.ETC);
        when(faceRecognitionPort.detectFaces(any(), anyString())).thenReturn(faceRecognitionWithFace);
        when(idVerificationUseCase.verifyIdCard(anyLong()))
                .thenReturn(IdVerification.builder().isIdCard(false).build());
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted");

        ImageUploadUseCase.UploadResult result = imageUploadService.uploadAndProcess(mockFile);

        assertThat(result.hasFace()).isTrue();
        verify(faceRecognitionPort).detectFaces(any(), anyString());
    }

    @Test
    @DisplayName("이미지 업로드 및 처리 성공 - 신분증 감지")
    void uploadAndProcess_Success_WithIdCard() throws IOException {
        IdVerification idVerification = IdVerification.builder()
                .isIdCard(true)
                .documentType("national_id")
                .extractedInfo(IdVerification.ExtractedInfo.builder()
                        .name("홍길동")
                        .idNumber("900101-1234567")
                        .build())
                .build();

        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(storagePort.uploadFile(any(), anyString(), anyString(), anyLong()))
                .thenReturn("1234567890_test.jpg");
        when(storagePort.downloadFile(anyString()))
                .thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
        when(ocrServicePort.extractText(any(), anyString())).thenReturn(ocrResult);
        when(documentTypeDetectionPort.detectDocumentType(any(), anyString(), anyString()))
                .thenReturn(DocumentType.ID_CARD);
        when(faceRecognitionPort.detectFaces(any(), anyString())).thenReturn(faceRecognition);
        when(idVerificationUseCase.verifyIdCard(anyLong())).thenReturn(idVerification);
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted");

        ImageUploadUseCase.UploadResult result = imageUploadService.uploadAndProcess(mockFile);

        assertThat(result.isIdCard()).isTrue();
        assertThat(result.extractedInfo()).isNotNull();
        verify(idVerificationUseCase).verifyIdCard(anyLong());
        verify(encryptionService, times(2)).encrypt(anyString());
    }

    @Test
    @DisplayName("이미지 업로드 실패 - IOException 발생")
    void uploadAndProcess_Failure_IOException() throws IOException {
        when(mockFile.getInputStream()).thenThrow(new IOException("File read error"));

        assertThatThrownBy(() -> imageUploadService.uploadAndProcess(mockFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("File upload failed");

        verify(storagePort, never()).uploadFile(any(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("이미지 업로드 - OCR 텍스트 없음")
    void uploadAndProcess_NoOcrText() throws IOException {
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(storagePort.uploadFile(any(), anyString(), anyString(), anyLong()))
                .thenReturn("1234567890_test.jpg");
        when(storagePort.downloadFile(anyString()))
                .thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
        when(ocrServicePort.extractText(any(), anyString())).thenReturn(
                OcrResult.builder().text("").confidence(0.0).language("unknown").boundingBoxes(new ArrayList<>()).build());
        when(documentTypeDetectionPort.detectDocumentType(any(), anyString(), anyString()))
                .thenReturn(DocumentType.ETC);
        when(faceRecognitionPort.detectFaces(any(), anyString())).thenReturn(faceRecognition);
        when(idVerificationUseCase.verifyIdCard(anyLong()))
                .thenReturn(IdVerification.builder().isIdCard(false).build());

        ImageUploadUseCase.UploadResult result = imageUploadService.uploadAndProcess(mockFile);

        assertThat(result.ocrText()).isEmpty();
        verify(encryptionService, never()).encrypt(anyString());
    }

    @Test
    @DisplayName("비동기 OCR 처리 - 병렬 실행 확인")
    void uploadAndProcess_AsyncOcrProcessing() throws IOException {
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(storagePort.uploadFile(any(), anyString(), anyString(), anyLong()))
                .thenReturn("1234567890_test.jpg");
        when(storagePort.downloadFile(anyString()))
                .thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
        when(ocrServicePort.extractText(any(), anyString())).thenReturn(ocrResult);
        when(documentTypeDetectionPort.detectDocumentType(any(), anyString(), anyString()))
                .thenReturn(DocumentType.ETC);
        when(faceRecognitionPort.detectFaces(any(), anyString())).thenReturn(faceRecognition);
        when(idVerificationUseCase.verifyIdCard(anyLong()))
                .thenReturn(IdVerification.builder().isIdCard(false).build());
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted_text");

        ImageUploadUseCase.UploadResult result = imageUploadService.uploadAndProcess(mockFile);

        assertThat(result).isNotNull();
        verify(storagePort, atLeast(2)).downloadFile(anyString());
    }

    @Test
    @DisplayName("비동기 얼굴 인식 처리 - 병렬 실행 확인")
    void uploadAndProcess_AsyncFaceRecognition() throws IOException {
        FaceRecognition faceRecognitionWithFace = FaceRecognition.builder()
                .hasFace(true)
                .faceCount(1)
                .faces(new ArrayList<>())
                .build();

        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(storagePort.uploadFile(any(), anyString(), anyString(), anyLong()))
                .thenReturn("1234567890_test.jpg");
        when(storagePort.downloadFile(anyString()))
                .thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
        when(ocrServicePort.extractText(any(), anyString())).thenReturn(ocrResult);
        when(documentTypeDetectionPort.detectDocumentType(any(), anyString(), anyString()))
                .thenReturn(DocumentType.ETC);
        when(faceRecognitionPort.detectFaces(any(), anyString())).thenReturn(faceRecognitionWithFace);
        when(idVerificationUseCase.verifyIdCard(anyLong()))
                .thenReturn(IdVerification.builder().isIdCard(false).build());
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted");

        ImageUploadUseCase.UploadResult result = imageUploadService.uploadAndProcess(mockFile);

        assertThat(result.hasFace()).isTrue();
        verify(faceRecognitionPort).detectFaces(any(), anyString());
    }

    @Test
    @DisplayName("비동기 처리 실패 시 기본값 반환")
    void uploadAndProcess_AsyncFailure_Fallback() throws IOException {
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(storagePort.uploadFile(any(), anyString(), anyString(), anyLong()))
                .thenReturn("1234567890_test.jpg");
        when(storagePort.downloadFile(anyString()))
                .thenThrow(new RuntimeException("Download failed"))
                .thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
        when(idVerificationUseCase.verifyIdCard(anyLong()))
                .thenReturn(IdVerification.builder().isIdCard(false).build());

        ImageUploadUseCase.UploadResult result = imageUploadService.uploadAndProcess(mockFile);

        assertThat(result).isNotNull();
        assertThat(result.imageId()).isEqualTo(1L);
    }
}

