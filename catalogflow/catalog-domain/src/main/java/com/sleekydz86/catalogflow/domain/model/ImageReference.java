package com.sleekydz86.catalogflow.domain.model;

import com.sleekydz86.catalogflow.domain.exception.InvalidImageReferenceException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ImageReference(
		String imageId,
		String storageKey,
		String contentType,
		long sizeInBytes,
		boolean temporary,
		Instant uploadedAt) {

	public ImageReference {
		if (imageId == null || imageId.isBlank()) {
			throw new InvalidImageReferenceException("이미지 ID는 비어 있을 수 없습니다");
		}
		if (storageKey == null || storageKey.isBlank()) {
			throw new InvalidImageReferenceException("저장소 키는 비어 있을 수 없습니다");
		}
		if (contentType == null || contentType.isBlank()) {
			throw new InvalidImageReferenceException("콘텐츠 타입은 비어 있을 수 없습니다");
		}
		if (sizeInBytes <= 0) {
			throw new InvalidImageReferenceException("이미지 크기는 0보다 커야 합니다");
		}
		Objects.requireNonNull(uploadedAt, "uploadedAt");
		imageId = imageId.trim();
		storageKey = storageKey.trim();
		contentType = contentType.trim().toLowerCase();
	}

	public static ImageReference create(
			String storageKey,
			String contentType,
			long sizeInBytes,
			boolean temporary,
			Instant uploadedAt) {
		return new ImageReference(UUID.randomUUID().toString(), storageKey, contentType, sizeInBytes, temporary,
				uploadedAt);
	}
}
