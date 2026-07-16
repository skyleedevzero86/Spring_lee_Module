package com.sleekydz86.catalogflow.adapter.out.storage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.port.out.StoragePort;
import com.sleekydz86.catalogflow.global.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FakeStorageAdapterTest {

	private FakeStorageAdapter adapter;

	@BeforeEach
	void setUp() {
		StorageProperties properties = new StorageProperties();
		adapter = new FakeStorageAdapter(
				properties,
				Clock.fixed(Instant.parse("2026-07-16T00:00:00Z"), ZoneOffset.UTC));
	}

	@Test
	void shouldCreatePresignedUploadAndMarkUploaded() {
		UUID productId = UUID.randomUUID();
		StoragePort.PresignedUpload upload = adapter.createPresignedUpload(
				productId,
				"image/png",
				"png",
				true);

		assertTrue(upload.storageKey().startsWith("temp/" + productId + "/"));
		assertTrue(upload.uploadUrl().contains("/fake-storage/upload/"));
		assertFalse(adapter.objectExists(upload.storageKey()));

		adapter.markUploaded(upload.storageKey());
		assertTrue(adapter.objectExists(upload.storageKey()));
	}

	@Test
	void shouldDeleteObject() {
		String storageKey = "products/" + UUID.randomUUID() + "/abc.png";
		adapter.simulateUpload(storageKey, "image/png", new byte[] {1, 2, 3});
		assertTrue(adapter.objectExists(storageKey));
		adapter.deleteObject(storageKey);
		assertFalse(adapter.objectExists(storageKey));
	}
}
