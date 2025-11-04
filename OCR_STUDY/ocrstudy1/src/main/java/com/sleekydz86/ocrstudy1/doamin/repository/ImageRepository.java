package com.sleekydz86.ocrstudy1.doamin.repository;

import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;

public interface ImageRepository {
    Image save(Image image);
    Optional<Image> findById(Long id);
    Optional<Image> findLatest();
    List<Image> findAll();
    Page<Image> findAll(Pageable pageable);
    Page<Image> findByDocumentType(DocumentType documentType, Pageable pageable);
    void deleteById(Long id);
}