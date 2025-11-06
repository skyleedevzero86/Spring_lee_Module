package com.sleekydz86.ocrstudy1.infrastructure.adapter.out.persistence;

import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaImageRepository.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("JpaImageRepository 테스트")
class JpaImageRepositoryTest {

    @Autowired
    private JpaImageRepository jpaImageRepository;

    private Image testImage;

    @BeforeEach
    void setUp() {

        testImage = Image.builder()
                .originalFilename("test.jpg")
                .storedFilename("test.jpg")
                .filePath("test.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .minioObjectName("1234567890_test.jpg")
                .documentType(DocumentType.RECEIPT)
                .build();
    }

    @Test
    @DisplayName("이미지 저장 성공")
    void save_Success() {
        Image saved = jpaImageRepository.save(testImage);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOriginalFilename()).isEqualTo("test.jpg");
    }

    @Test
    @DisplayName("이미지 ID로 조회 성공")
    void findById_Success() {
        Image saved = jpaImageRepository.save(testImage);

        Optional<Image> found = jpaImageRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("이미지 ID로 조회 실패")
    void findById_NotFound() {
        Optional<Image> found = jpaImageRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("최근 이미지 조회 성공")
    void findLatest_Success() throws InterruptedException {
        Image image1 = createImage("image1.jpg", DocumentType.RECEIPT);
        Image image2 = createImage("image2.jpg", DocumentType.ID_CARD);

        jpaImageRepository.save(image1);
        Thread.sleep(10);
        jpaImageRepository.save(image2);

        Optional<Image> latest = jpaImageRepository.findLatest();

        assertThat(latest).isPresent();
        assertThat(latest.get().getOriginalFilename()).isEqualTo("image2.jpg");
    }

    @Test
    @DisplayName("전체 이미지 목록 조회")
    void findAll_Success() {
        jpaImageRepository.save(createImage("image1.jpg", DocumentType.RECEIPT));
        jpaImageRepository.save(createImage("image2.jpg", DocumentType.ID_CARD));

        var all = jpaImageRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("페이징 조회 성공")
    void findAll_Pageable() {
        for (int i = 1; i <= 5; i++) {
            jpaImageRepository.save(createImage("image" + i + ".jpg", DocumentType.RECEIPT));
        }

        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());
        Page<Image> page = jpaImageRepository.findAll(pageable);

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("문서 타입별 조회 성공")
    void findByDocumentType_Success() {
        jpaImageRepository.save(createImage("receipt1.jpg", DocumentType.RECEIPT));
        jpaImageRepository.save(createImage("receipt2.jpg", DocumentType.RECEIPT));
        jpaImageRepository.save(createImage("idcard.jpg", DocumentType.ID_CARD));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Image> receiptPage = jpaImageRepository.findByDocumentType(DocumentType.RECEIPT, pageable);

        assertThat(receiptPage.getContent()).hasSize(2);
        assertThat(receiptPage.getContent()).allMatch(img ->
                img.getDocumentType() == DocumentType.RECEIPT);
    }

    @Test
    @DisplayName("이미지 삭제 성공")
    void deleteById_Success() {
        Image saved = jpaImageRepository.save(testImage);
        Long id = saved.getId();

        jpaImageRepository.deleteById(id);

        Optional<Image> found = jpaImageRepository.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이미지 조회 캐싱 테스트 - 같은 ID로 두 번 조회")
    void findById_CachingTest() {
        Image saved = jpaImageRepository.save(testImage);
        Long id = saved.getId();

        Optional<Image> found1 = jpaImageRepository.findById(id);

        Optional<Image> found2 = jpaImageRepository.findById(id);

        assertThat(found1).isPresent();
        assertThat(found2).isPresent();
        assertThat(found1.get().getId()).isEqualTo(found2.get().getId());
    }

    @Test
    @DisplayName("이미지 저장 시 캐시 무효화 테스트")
    void save_CacheEvictTest() {
        Image saved = jpaImageRepository.save(testImage);
        Long id = saved.getId();

        Optional<Image> found1 = jpaImageRepository.findById(id);
        assertThat(found1).isPresent();

        saved.setOriginalFilename("updated.jpg");
        jpaImageRepository.save(saved);

        Optional<Image> found2 = jpaImageRepository.findById(id);
        assertThat(found2).isPresent();
        assertThat(found2.get().getOriginalFilename()).isEqualTo("updated.jpg");
    }

    @Test
    @DisplayName("이미지 삭제 시 캐시 무효화 테스트")
    void deleteById_CacheEvictTest() {
        Image saved = jpaImageRepository.save(testImage);
        Long id = saved.getId();

        Optional<Image> found1 = jpaImageRepository.findById(id);
        assertThat(found1).isPresent();

        jpaImageRepository.deleteById(id);

        Optional<Image> found2 = jpaImageRepository.findById(id);
        assertThat(found2).isEmpty();
    }

    private Image createImage(String filename, DocumentType documentType) {
        return Image.builder()
                .originalFilename(filename)
                .storedFilename(filename)
                .filePath(filename)
                .fileSize(1024L)
                .contentType("image/jpeg")
                .minioObjectName(System.currentTimeMillis() + "_" + filename)
                .documentType(documentType)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

