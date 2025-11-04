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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService implements ImageUploadUseCase {

    private final ImageRepository imageRepository;
    private final StoragePort storagePort;
    private final OcrServicePort ocrServicePort;
    private final FaceRecognitionPort faceRecognitionPort;
    private final IdVerificationUseCase idVerificationUseCase;
    private final DocumentTypeDetectionPort documentTypeDetectionPort;
    private final EncryptionService encryptionService;

    @Override
    @Transactional
    public UploadResult uploadAndProcess(MultipartFile file) {
        try {

            String objectName = storagePort.uploadFile(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize()
            );

            Image image = Image.builder()
                    .originalFilename(file.getOriginalFilename())
                    .storedFilename(file.getOriginalFilename())
                    .filePath(objectName)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .minioObjectName(objectName)
                    .build();

            image = imageRepository.save(image);

            String finalObjectName = objectName;
            String finalFilename = file.getOriginalFilename();

            CompletableFuture<OcrResult> ocrFuture = processOcrAsync(finalObjectName, finalFilename);

            CompletableFuture<FaceRecognition> faceRecognitionFuture =
                    processFaceRecognitionAsync(finalObjectName, finalFilename);

            CompletableFuture.allOf(ocrFuture, faceRecognitionFuture).join();

            OcrResult ocrResult = ocrFuture.get();
            FaceRecognition faceRecognition = faceRecognitionFuture.get();

            String ocrText = "";
            if (ocrResult != null && ocrResult.getText() != null && !ocrResult.getText().isEmpty()) {
                ocrText = ocrResult.getText();
                image.updateOcrText(ocrText);

                String encryptedOcrText = encryptionService.encrypt(ocrText);
                image.setEncryptedOcrText(encryptedOcrText);

                log.info("OCR extracted text: {}", ocrText);

                DocumentType documentType = documentTypeDetectionPort.detectDocumentType(
                        storagePort.downloadFile(finalObjectName),
                        ocrText,
                        finalFilename
                );
                image.setDocumentType(documentType);
                log.info("Detected document type: {}", documentType);
            } else {
                image.setDocumentType(DocumentType.ETC);
            }


            if (faceRecognition.getHasFace() != null && faceRecognition.getHasFace()) {
                String faceEncoding = faceRecognition.getFaces().isEmpty()
                        ? ""
                        : faceRecognition.getFaces().get(0).getEncoding();
                image.updateFaceEncoding(faceEncoding);
                log.info("Face detected: count={}", faceRecognition.getFaceCount());
            }


            IdVerification idVerification = idVerificationUseCase.verifyIdCard(image.getId());
            String extractedInfo = null;
            if (idVerification.getIsIdCard() != null && idVerification.getIsIdCard()) {
                if (idVerification.getExtractedInfo() != null) {
                    extractedInfo = formatIdInfo(idVerification.getExtractedInfo());

                    String encryptedExtractedInfo = encryptionService.encrypt(extractedInfo);
                    image.setEncryptedExtractedIdInfo(encryptedExtractedInfo);
                }
                image.markAsIdCard(extractedInfo);
                log.info("ID Card verified: type={}", idVerification.getDocumentType());
            }

            image = imageRepository.save(image);

            return new UploadResult(
                    image.getId(),
                    image.getOriginalFilename(),
                    ocrText,
                    faceRecognition.getHasFace() != null && faceRecognition.getHasFace(),
                    idVerification.getIsIdCard() != null && idVerification.getIsIdCard(),
                    extractedInfo
            );

        } catch (IOException | ExecutionException | InterruptedException e) {
            log.error("File upload processing failed", e);
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Async("ocrExecutor")
    public CompletableFuture<OcrResult> processOcrAsync(String objectName, String filename) {
        try {
            log.info("Starting async OCR processing for: {}", filename);
            InputStream imageStream = storagePort.downloadFile(objectName);
            OcrResult ocrResult = ocrServicePort.extractText(imageStream, filename);
            log.info("Completed async OCR processing for: {}", filename);
            return CompletableFuture.completedFuture(ocrResult);
        } catch (Exception e) {
            log.error("Async OCR processing failed for: {}", filename, e);
            return CompletableFuture.completedFuture(null);
        }
    }

    @Async("faceRecognitionExecutor")
    public CompletableFuture<FaceRecognition> processFaceRecognitionAsync(String objectName, String filename) {
        try {
            log.info("Starting async face recognition for: {}", filename);
            InputStream imageStream = storagePort.downloadFile(objectName);
            FaceRecognition faceRecognition = faceRecognitionPort.detectFaces(imageStream, filename);
            log.info("Completed async face recognition for: {}", filename);
            return CompletableFuture.completedFuture(faceRecognition);
        } catch (Exception e) {
            log.error("Async face recognition failed for: {}", filename, e);
            return CompletableFuture.completedFuture(
                    FaceRecognition.builder()
                            .hasFace(false)
                            .faceCount(0)
                            .faces(java.util.List.of())
                            .build()
            );
        }
    }

    private String formatIdInfo(IdVerification.ExtractedInfo info) {
        StringBuilder sb = new StringBuilder();
        if (info.getName() != null) sb.append("이름: ").append(info.getName()).append("\n");
        if (info.getIdNumber() != null) sb.append("주민번호: ").append(info.getIdNumber()).append("\n");
        if (info.getDateOfBirth() != null) sb.append("생년월일: ").append(info.getDateOfBirth()).append("\n");
        if (info.getAddress() != null) sb.append("주소: ").append(info.getAddress()).append("\n");
        return sb.toString();
    }
}

