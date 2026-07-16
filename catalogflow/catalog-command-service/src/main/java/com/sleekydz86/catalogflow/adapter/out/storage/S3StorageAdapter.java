package com.sleekydz86.catalogflow.adapter.out.storage;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.port.out.StoragePort;
import com.sleekydz86.catalogflow.global.config.StorageProperties;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3", matchIfMissing = true)
public class S3StorageAdapter implements StoragePort {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final StorageProperties storageProperties;
	private final Clock clock;

	public S3StorageAdapter(
			S3Client s3Client,
			S3Presigner s3Presigner,
			StorageProperties storageProperties,
			Clock clock) {
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
		this.storageProperties = storageProperties;
		this.clock = clock;
	}

	@Override
	public PresignedUpload createPresignedUpload(
			UUID productId,
			String contentType,
			String fileExtension,
			boolean temporary) {
		try {
			String storageKey = ObjectKeyFactory.create(productId, fileExtension, temporary);
			Duration duration = Duration.ofSeconds(storageProperties.getPresignDurationSeconds());
			Instant expiresAt = clock.instant().plus(duration);
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(storageProperties.getBucket())
					.key(storageKey)
					.contentType(contentType)
					.build();
			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
					.signatureDuration(duration)
					.putObjectRequest(putObjectRequest)
					.build();
			String uploadUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
			String downloadUrl = createPresignedDownload(storageKey);
			return new PresignedUpload(storageKey, uploadUrl, downloadUrl, expiresAt);
		}
		catch (Exception exception) {
			throw new ApplicationException("사전 서명 업로드 URL 생성에 실패했습니다", exception);
		}
	}

	@Override
	public String createPresignedDownload(String storageKey) {
		try {
			Duration duration = Duration.ofSeconds(storageProperties.getPresignDurationSeconds());
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
					.bucket(storageProperties.getBucket())
					.key(storageKey)
					.build();
			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
					.signatureDuration(duration)
					.getObjectRequest(getObjectRequest)
					.build();
			return s3Presigner.presignGetObject(presignRequest).url().toString();
		}
		catch (Exception exception) {
			throw new ApplicationException("사전 서명 다운로드 URL 생성에 실패했습니다", exception);
		}
	}

	@Override
	public void deleteObject(String storageKey) {
		try {
			s3Client.deleteObject(DeleteObjectRequest.builder()
					.bucket(storageProperties.getBucket())
					.key(storageKey)
					.build());
		}
		catch (Exception exception) {
			throw new ApplicationException("객체 삭제에 실패했습니다", exception);
		}
	}

	@Override
	public boolean objectExists(String storageKey) {
		try {
			s3Client.headObject(HeadObjectRequest.builder()
					.bucket(storageProperties.getBucket())
					.key(storageKey)
					.build());
			return true;
		}
		catch (NoSuchKeyException exception) {
			return false;
		}
		catch (S3Exception exception) {
			if (exception.statusCode() == 404) {
				return false;
			}
			throw new ApplicationException("객체 존재 여부 확인에 실패했습니다", exception);
		}
		catch (Exception exception) {
			throw new ApplicationException("객체 존재 여부 확인에 실패했습니다", exception);
		}
	}
}
