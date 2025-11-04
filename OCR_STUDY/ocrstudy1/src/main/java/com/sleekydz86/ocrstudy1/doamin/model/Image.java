package com.sleekydz86.ocrstudy1.doamin.model;

import jakarta.persistence.*;
import lombok.*;
import org.w3c.dom.DocumentType;
import java.time.LocalDateTime;

@Entity
@Table(name = "images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String storedFilename;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String minioObjectName;

    @Column(length = 4000)
    private String ocrText;

    @Column(length = 4000)
    private String encryptedOcrText;

    private String faceEncoding;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    private Boolean isIdCard;

    @Column(length = 2000)
    private String extractedIdInfo;

    @Column(length = 2000)
    private String encryptedExtractedIdInfo;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void updateOcrText(String ocrText) {
        this.ocrText = ocrText;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateFaceEncoding(String faceEncoding) {
        this.faceEncoding = faceEncoding;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsIdCard(String extractedIdInfo) {
        this.isIdCard = true;
        this.extractedIdInfo = extractedIdInfo;
        this.updatedAt = LocalDateTime.now();
    }
}
