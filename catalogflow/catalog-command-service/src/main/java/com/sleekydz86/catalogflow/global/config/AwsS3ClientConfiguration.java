package com.sleekydz86.catalogflow.global.config;

import java.net.URI;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3", matchIfMissing = true)
public class AwsS3ClientConfiguration {

	@Bean(destroyMethod = "close")
	S3Client s3Client(StorageProperties storageProperties) {
		var builder = S3Client.builder()
				.region(Region.of(storageProperties.getRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(
								storageProperties.getAccessKey(),
								storageProperties.getSecretKey())))
				.httpClientBuilder(UrlConnectionHttpClient.builder())
				.serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
						.pathStyleAccessEnabled(storageProperties.isPathStyleAccess())
						.build());
		if (storageProperties.getEndpoint() != null && !storageProperties.getEndpoint().isBlank()) {
			builder.endpointOverride(URI.create(storageProperties.getEndpoint()));
		}
		return builder.build();
	}

	@Bean(destroyMethod = "close")
	S3Presigner s3Presigner(StorageProperties storageProperties) {
		var builder = S3Presigner.builder()
				.region(Region.of(storageProperties.getRegion()))
				.credentialsProvider(StaticCredentialsProvider.create(
						AwsBasicCredentials.create(
								storageProperties.getAccessKey(),
								storageProperties.getSecretKey())))
				.serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
						.pathStyleAccessEnabled(storageProperties.isPathStyleAccess())
						.build());
		if (storageProperties.getEndpoint() != null && !storageProperties.getEndpoint().isBlank()) {
			builder.endpointOverride(URI.create(storageProperties.getEndpoint()));
		}
		return builder.build();
	}
}
