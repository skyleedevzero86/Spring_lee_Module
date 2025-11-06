package com.sleekydz86.ocrstudy1.integration;

import com.sleekydz86.ocrstudy1.application.port.in.ImageUploadUseCase;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "minio.endpoint=http://localhost:9000",
        "minio.access-key=minioadmin",
        "minio.secret-key=minioadmin",
        "minio.bucket-name=test-bucket",
        "encryption.key=MySecretKey123456789012345678901234567890",
        "spring.ai.openai.api-key=test-key"
})
@Transactional
@DisplayName("이미지 업로드 통합 테스트")
class ImageUploadIntegrationTest {

    @Autowired
    private ImageUploadUseCase imageUploadUseCase;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    @DisplayName("이미지 업로드 전체 플로우 통합 테스트")
    void uploadImage_IntegrationFlow() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                createTestImageBytes());

        ImageUploadUseCase.UploadResult result = imageUploadUseCase.uploadAndProcess(file);

        assertThat(result).isNotNull();
        assertThat(result.imageId()).isNotNull();
        assertThat(result.filename()).isEqualTo("test.jpg");

        Optional<Image> savedImage = imageRepository.findById(result.imageId());
        assertThat(savedImage).isPresent();
        assertThat(savedImage.get().getOriginalFilename()).isEqualTo("test.jpg");
    }

    private byte[] createTestImageBytes() {
        return new byte[] { (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A,
                (byte) 0x0A };
    }
}
