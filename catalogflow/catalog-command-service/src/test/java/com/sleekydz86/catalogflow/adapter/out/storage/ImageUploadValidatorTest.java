package com.sleekydz86.catalogflow.adapter.out.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.sleekydz86.catalogflow.global.config.StorageProperties;
import com.sleekydz86.catalogflow.global.exception.InvalidImageUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImageUploadValidatorTest {

	private ImageUploadValidator validator;

	@BeforeEach
	void setUp() {
		StorageProperties properties = new StorageProperties();
		validator = new ImageUploadValidator(properties);
	}

	@Test
	@DisplayName("유효한 JPEG 업로드는 통과한다")
	void shouldAcceptValidJpegUpload() {
		// given / when / then
		assertDoesNotThrow(() -> validator.validate("image/jpeg", 1024L, "photo.jpg"));
		assertEquals("jpg", validator.resolveExtension("photo.jpg", "image/jpeg"));
	}

	@Test
	@DisplayName("허용되지 않은 콘텐츠 타입은 거부한다")
	void shouldRejectDisallowedContentType() {
		// given / when / then
		InvalidImageUploadException exception = assertThrows(
				InvalidImageUploadException.class,
				() -> validator.validate("application/pdf", 1024L, "doc.pdf"));
		assertTrue(exception.getMessage().contains("허용되지 않은 콘텐츠 타입"));
	}

	@Test
	@DisplayName("콘텐츠 타입과 확장자가 다르면 거부한다")
	void shouldRejectMismatchedExtension() {
		// given / when / then
		InvalidImageUploadException exception = assertThrows(
				InvalidImageUploadException.class,
				() -> validator.validate("image/png", 1024L, "photo.jpg"));
		assertTrue(exception.getMessage().contains("콘텐츠 타입과 확장자가 일치하지 않습니다"));
	}

	@Test
	@DisplayName("파일 크기 제한 초과는 거부한다")
	void shouldRejectOversizedFile() {
		// given / when / then
		InvalidImageUploadException exception = assertThrows(
				InvalidImageUploadException.class,
				() -> validator.validate("image/png", 20_000_000L, "photo.png"));
		assertTrue(exception.getMessage().contains("파일 크기가 제한을 초과"));
	}

	@Test
	@DisplayName("다른 상품의 저장소 키는 거부한다")
	void shouldRejectStorageKeyForOtherProduct() {
		// given
		UUID productId = UUID.randomUUID();

		// when / then
		InvalidImageUploadException exception = assertThrows(
				InvalidImageUploadException.class,
				() -> validator.validateStorageKeyBelongsToProduct(
						"temp/" + UUID.randomUUID() + "/abc.png",
						productId));
		assertTrue(exception.getMessage().contains("해당 상품에 속하지 않습니다"));
	}

	@Test
	@DisplayName("경로 조작이 포함된 저장소 키는 거부한다")
	void shouldRejectPathTraversalStorageKey() {
		// given
		UUID productId = UUID.randomUUID();

		// when / then
		InvalidImageUploadException exception = assertThrows(
				InvalidImageUploadException.class,
				() -> validator.validateStorageKeyBelongsToProduct(
						"temp/" + productId + "/../secret.png",
						productId));
		assertTrue(exception.getMessage().contains("저장소 키가 올바르지 않습니다"));
	}
}
