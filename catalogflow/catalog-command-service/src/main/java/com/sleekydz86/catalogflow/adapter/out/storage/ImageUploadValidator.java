package com.sleekydz86.catalogflow.adapter.out.storage;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.sleekydz86.catalogflow.global.config.StorageProperties;
import com.sleekydz86.catalogflow.global.exception.InvalidImageUploadException;
import org.springframework.stereotype.Component;

@Component
public class ImageUploadValidator {

	private static final Map<String, Set<String>> CONTENT_TYPE_EXTENSIONS = Map.of(
			"image/jpeg", Set.of("jpg", "jpeg"),
			"image/png", Set.of("png"),
			"image/webp", Set.of("webp"),
			"image/gif", Set.of("gif"));

	private final StorageProperties storageProperties;

	public ImageUploadValidator(StorageProperties storageProperties) {
		this.storageProperties = storageProperties;
	}

	public void validate(String contentType, long sizeInBytes, String fileName) {
		if (contentType == null || contentType.isBlank()) {
			throw new InvalidImageUploadException("콘텐츠 타입은 필수입니다");
		}
		String normalizedContentType = contentType.trim().toLowerCase(Locale.ROOT);
		if (!storageProperties.getAllowedContentTypes().contains(normalizedContentType)) {
			throw new InvalidImageUploadException("허용되지 않은 콘텐츠 타입입니다: " + contentType);
		}
		if (sizeInBytes <= 0) {
			throw new InvalidImageUploadException("파일 크기는 0보다 커야 합니다");
		}
		if (sizeInBytes > storageProperties.getMaxFileSizeBytes()) {
			throw new InvalidImageUploadException(
					"파일 크기가 제한을 초과했습니다. 최대 "
							+ storageProperties.getMaxFileSizeBytes()
							+ "바이트까지 허용됩니다");
		}
		String extension = resolveExtension(fileName, normalizedContentType);
		Set<String> allowedExtensions = CONTENT_TYPE_EXTENSIONS.get(normalizedContentType);
		if (allowedExtensions == null || !allowedExtensions.contains(extension)) {
			throw new InvalidImageUploadException(
					"콘텐츠 타입과 확장자가 일치하지 않습니다: " + contentType + " / " + extension);
		}
	}

	public String resolveExtension(String fileName, String contentType) {
		String normalizedContentType = contentType.trim().toLowerCase(Locale.ROOT);
		if (fileName != null && !fileName.isBlank()) {
			int index = fileName.lastIndexOf('.');
			if (index < 0 || index == fileName.length() - 1) {
				throw new InvalidImageUploadException("파일 확장자가 필요합니다");
			}
			String extension = fileName.substring(index + 1).toLowerCase(Locale.ROOT);
			if (extension.contains("/") || extension.contains("\\") || extension.contains("..")) {
				throw new InvalidImageUploadException("파일 이름이 올바르지 않습니다");
			}
			return extension;
		}
		return switch (normalizedContentType) {
			case "image/jpeg" -> "jpg";
			case "image/png" -> "png";
			case "image/webp" -> "webp";
			case "image/gif" -> "gif";
			default -> throw new InvalidImageUploadException("확장자를 결정할 수 없습니다");
		};
	}

	public void validateStorageKeyBelongsToProduct(String storageKey, UUID productId) {
		if (storageKey == null || storageKey.isBlank()) {
			throw new InvalidImageUploadException("저장소 키는 필수입니다");
		}
		String expectedPrefixTemp = "temp/" + productId + "/";
		String expectedPrefixConfirmed = "products/" + productId + "/";
		if (!storageKey.startsWith(expectedPrefixTemp) && !storageKey.startsWith(expectedPrefixConfirmed)) {
			throw new InvalidImageUploadException("저장소 키가 해당 상품에 속하지 않습니다");
		}
		if (storageKey.contains("..") || storageKey.startsWith("/")) {
			throw new InvalidImageUploadException("저장소 키가 올바르지 않습니다");
		}
	}
}
