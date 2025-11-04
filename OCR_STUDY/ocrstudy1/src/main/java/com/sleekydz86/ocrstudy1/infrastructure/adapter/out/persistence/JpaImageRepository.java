package com.sleekydz86.ocrstudy1.infrastructure.adapter.out.persistence;

import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaImageRepository implements ImageRepository {

    private final SpringDataImageRepository springDataImageRepository;

    public JpaImageRepository(SpringDataImageRepository springDataImageRepository) {
        this.springDataImageRepository = springDataImageRepository;
    }

    @Override
    @CacheEvict(value = {"images", "ocrResults"}, key = "#image.id", condition = "#image.id != null")
    public Image save(Image image) {
        Image saved = springDataImageRepository.save(image);
        if (image.getId() == null) {
        }
        return saved;
    }

    @Override
    @Cacheable(value = "images", key = "#id", unless = "#result.isEmpty()")
    public Optional<Image> findById(Long id) {
        return springDataImageRepository.findById(id);
    }

    @Override
    @Cacheable(value = "images", key = "'latest'")
    public Optional<Image> findLatest() {
        return springDataImageRepository.findTopByOrderByCreatedAtDesc();
    }

    @Override
    public List<Image> findAll() {
        return springDataImageRepository.findAll();
    }

    @Override
    public Page<Image> findAll(Pageable pageable) {
        return springDataImageRepository.findAll(pageable);
    }

    @Override
    public Page<Image> findByDocumentType(DocumentType documentType, Pageable pageable) {
        return springDataImageRepository.findByDocumentType(documentType, pageable);
    }

    @Override
    @CacheEvict(value = {"images", "ocrResults"}, key = "#id")
    public void deleteById(Long id) {
        springDataImageRepository.deleteById(id);
    }

    interface SpringDataImageRepository extends JpaRepository<Image, Long> {
        Optional<Image> findTopByOrderByCreatedAtDesc();
        Page<Image> findByDocumentType(DocumentType documentType, Pageable pageable);
    }
}

