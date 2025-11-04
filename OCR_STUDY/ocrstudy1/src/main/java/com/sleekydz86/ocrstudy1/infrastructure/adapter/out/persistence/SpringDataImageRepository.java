package com.sleekydz86.ocrstudy1.infrastructure.adapter.out.persistence;

import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findTopByOrderByCreatedAtDesc();
    Page<Image> findByDocumentType(DocumentType documentType, Pageable pageable);
}

