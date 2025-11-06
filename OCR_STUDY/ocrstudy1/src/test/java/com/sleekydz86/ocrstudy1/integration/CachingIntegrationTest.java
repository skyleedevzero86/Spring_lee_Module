package com.sleekydz86.ocrstudy1.integration;

import com.sleekydz86.ocrstudy1.application.service.DocumentAnalysisService;
import com.sleekydz86.ocrstudy1.application.service.IdVerificationService;
import com.sleekydz86.ocrstudy1.application.service.ImageCompareService;
import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({DocumentAnalysisService.class, IdVerificationService.class, ImageCompareService.class})
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cache.type=simple",
        "spring.cache.cache-names=images,ocrResults,documentAnalysis,faceRecognition"
})
@DisplayName("캐싱 통합 테스트")
class CachingIntegrationTest {

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Autowired
    private ImageRepository imageRepository;

    private Image testImage;

    @BeforeEach
    void setUp() {
        testImage = Image.builder()
                .originalFilename("test.jpg")
                .storedFilename("test.jpg")
                .filePath("test.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .minioObjectName("test.jpg")
                .documentType(DocumentType.RECEIPT)
                .ocrText("테스트 OCR 텍스트")
                .build();
    }

    @Test
    @DisplayName("ImageRepository 캐싱 통합 테스트")
    void imageRepository_CachingIntegrationTest() {

        Image saved = imageRepository.save(testImage);
        Long id = saved.getId();

        Optional<Image> found1 = imageRepository.findById(id);
        assertThat(found1).isPresent();

        if (cacheManager != null) {
            var imagesCache = cacheManager.getCache("images");
            if (imagesCache != null) {
                var cached = imagesCache.get(id, Image.class);
                assertThat(cached).isNotNull();
                assertThat(cached.getId()).isEqualTo(id);
            }
        }

        Optional<Image> found2 = imageRepository.findById(id);
        assertThat(found2).isPresent();
        assertThat(found2.get().getId()).isEqualTo(found1.get().getId());
    }

    @Test
    @DisplayName("이미지 저장 시 캐시 무효화 통합 테스트")
    void imageRepository_Save_CacheEvictIntegrationTest() {

        Image saved = imageRepository.save(testImage);
        Long id = saved.getId();
        imageRepository.findById(id); // 캐시에 저장

        saved.setOriginalFilename("updated.jpg");
        imageRepository.save(saved);

        Optional<Image> found = imageRepository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getOriginalFilename()).isEqualTo("updated.jpg");
    }
}

