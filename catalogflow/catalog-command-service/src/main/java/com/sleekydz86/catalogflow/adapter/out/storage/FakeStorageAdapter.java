package com.sleekydz86.catalogflow.adapter.out.storage;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.sleekydz86.catalogflow.application.port.out.StoragePort;
import com.sleekydz86.catalogflow.global.config.StorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "fake")
public class FakeStorageAdapter implements StoragePort {

	private final StorageProperties storageProperties;
	private final Clock clock;
	private final Map<String, StoredObject> objects = new ConcurrentHashMap<>();

	public FakeStorageAdapter(StorageProperties storageProperties, Clock clock) {
		this.storageProperties = storageProperties;
		this.clock = clock;
	}

	@Override
	public PresignedUpload createPresignedUpload(
			UUID productId,
			String contentType,
			String fileExtension,
			boolean temporary) {
		String storageKey = ObjectKeyFactory.create(productId, fileExtension, temporary);
		Instant expiresAt = clock.instant().plus(Duration.ofSeconds(storageProperties.getPresignDurationSeconds()));
		objects.putIfAbsent(storageKey, new StoredObject(contentType, new byte[0], false));
		String uploadUrl = "http://localhost/fake-storage/upload/" + storageKey + "?expires=" + expiresAt;
		String downloadUrl = "http://localhost/fake-storage/download/" + storageKey;
		return new PresignedUpload(storageKey, uploadUrl, downloadUrl, expiresAt);
	}

	@Override
	public String createPresignedDownload(String storageKey) {
		return "http://localhost/fake-storage/download/" + storageKey;
	}

	@Override
	public void deleteObject(String storageKey) {
		objects.remove(storageKey);
	}

	@Override
	public boolean objectExists(String storageKey) {
		StoredObject object = objects.get(storageKey);
		return object != null && object.uploaded();
	}

	public void simulateUpload(String storageKey, String contentType, byte[] bytes) {
		objects.put(storageKey, new StoredObject(contentType, bytes == null ? new byte[0] : bytes, true));
	}

	public void markUploaded(String storageKey) {
		StoredObject existing = objects.get(storageKey);
		if (existing == null) {
			objects.put(storageKey, new StoredObject("application/octet-stream", new byte[0], true));
			return;
		}
		objects.put(storageKey, new StoredObject(existing.contentType(), existing.bytes(), true));
	}

	private record StoredObject(String contentType, byte[] bytes, boolean uploaded) {
	}
}
