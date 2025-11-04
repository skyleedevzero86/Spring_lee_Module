package com.sleekydz86.ocrstudy1.application.service;

import com.sleekydz86.ocrstudy1.application.port.in.ImageCompareUseCase;
import com.sleekydz86.ocrstudy1.application.port.out.FaceRecognitionPort;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import com.sleekydz86.ocrstudy1.doamin.model.ImageComparison;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCompareService implements ImageCompareUseCase {

    private final ImageRepository imageRepository;
    private final FaceRecognitionPort faceRecognitionPort;

    @Override
    @Cacheable(value = "faceRecognition", key = "'compare:latest:' + #currentImageId")
    public ImageComparison compareWithLatest(Long currentImageId) {
        Image currentImage = imageRepository.findById(currentImageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + currentImageId));

        Optional<Image> latestImageOpt = imageRepository.findLatest();

        if (latestImageOpt.isEmpty() || latestImageOpt.get().getId().equals(currentImageId)) {
            return ImageComparison.builder()
                    .currentImageId(currentImageId)
                    .similarityScore(0.0)
                    .comparisonType(ImageComparison.ComparisonType.VISUAL_SIMILARITY)
                    .details("No previous image to compare")
                    .build();
        }

        Image latestImage = latestImageOpt.get();
        return compareImages(currentImage, latestImage);
    }

    @Override
    @Cacheable(value = "faceRecognition", key = "'compare:' + #imageId1 + ':' + #imageId2")
    public ImageComparison compareImages(Long imageId1, Long imageId2) {
        Image image1 = imageRepository.findById(imageId1)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId1));
        Image image2 = imageRepository.findById(imageId2)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId2));

        return compareImages(image1, image2);
    }

    private ImageComparison compareImages(Image image1, Image image2) {
        Double faceSimilarity = 0.0;
        Double textSimilarity = 0.0;
        ImageComparison.ComparisonType comparisonType = ImageComparison.ComparisonType.VISUAL_SIMILARITY;

        if (image1.getFaceEncoding() != null &&
                !image1.getFaceEncoding().isEmpty() &&
                image2.getFaceEncoding() != null &&
                !image2.getFaceEncoding().isEmpty()) {

            faceSimilarity = faceRecognitionPort.compareFaces(
                    image1.getFaceEncoding(),
                    image2.getFaceEncoding()
            );
            comparisonType = ImageComparison.ComparisonType.FACE_SIMILARITY;
            log.info("Face similarity: {}", faceSimilarity);
        }

        if (image1.getOcrText() != null &&
                !image1.getOcrText().isEmpty() &&
                image2.getOcrText() != null &&
                !image2.getOcrText().isEmpty()) {

            textSimilarity = calculateTextSimilarity(
                    image1.getOcrText(),
                    image2.getOcrText()
            );
            log.info("Text similarity: {}", textSimilarity);
        }

        Double finalSimilarity = faceSimilarity > 0 ? faceSimilarity : textSimilarity;
        if (finalSimilarity > 0 && faceSimilarity > 0) {
            comparisonType = ImageComparison.ComparisonType.FACE_SIMILARITY;
        } else if (textSimilarity > 0) {
            comparisonType = ImageComparison.ComparisonType.OCR_TEXT_SIMILARITY;
        }

        String details = String.format(
                "Face similarity: %.2f, Text similarity: %.2f",
                faceSimilarity,
                textSimilarity
        );

        return ImageComparison.builder()
                .currentImageId(image1.getId())
                .previousImageId(image2.getId())
                .similarityScore(finalSimilarity)
                .comparisonType(comparisonType)
                .details(details)
                .build();
    }

    private Double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }

        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");

        int intersection = 0;
        int union = words1.length + words2.length;

        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2)) {
                    intersection++;
                    break;
                }
            }
        }

        if (union == 0) {
            return 0.0;
        }

        return (double) intersection / (union - intersection);
    }
}

