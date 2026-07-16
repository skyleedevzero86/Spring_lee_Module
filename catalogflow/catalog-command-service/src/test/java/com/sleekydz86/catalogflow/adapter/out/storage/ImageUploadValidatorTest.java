package com.sleekydz86.catalogflow.adapter.out.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.sleekydz86.catalogflow.global.config.StorageProperties;
import com.sleekydz86.catalogflow.global.exception.InvalidImageUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ImageUploadValidatorTest {

	private ImageUploadValidator validator;

	@BeforeEach
	void setUp() {
		StorageProperties properties = new StorageProperties();
		validator = new ImageUploadValidator(properties);
	}

	@Test
	void shouldAcceptValidJpegUpload() {
		assertDoesNotThrow(() -> validator.validate("image/jpeg", 1024L, "photo.jpg"));
		assertEquals("jpg", validator.resolveExtension("photo.jpg", "image/jpeg"));
	}

	@Test
	void shouldRejectDisallowedContentType() {
		InvalidImageUploadException exception = assertThrows(
				InvalidImageUploadException.class,
				() -> validator.validate("application/pdf", 1024L, "doc.pdf"));
		assertTrue(exception.getMessage().contains("허용되지 않은 콘텐츠 타입"));
	}

	@Test
	void shouldRejectMismatchedExtension() {
		InvalidImageUploadException exception = assertThrows(
				InvalidImageUploadException.class,
				() -> validator.validate("image/png", 1024L, "photo.jpg"));
		assertTrue(exception.getMessage().contains("콘텐츠 타입과 확장자가 일치하지 않습니다"));
	}

	@Test
	void shouldRejectOversizedFile() {
		InvalidImageUploadException exception = assertThrows(
				InvalidImageUploadException.class,
				() -> validator.validate("image/png", 20_000_000L, "photo.png"));
		assertTrue(exception.getMessage().contains("파일 크기가 제한을 초과"));
	}

	@Test
	void shouldRejectStorageKeyForOtherProduct() {
		UUID productId = UUID.randomUUID();
		InvalidImageUploadException exception = assertThrows(
				InvalidImageUploadException.class,
				() -> validator.validateStorageKeyBelongsToProduct(
						"temp/" + UUID.randomUUID() + "/abc.png",
						productId));
		assertTrue(exception.getMessage().contains("해당 상품에 속하지 않습니다"));
	}
}
