package com.sleekydz86.catalogflow.application.service;

import com.sleekydz86.catalogflow.adapter.out.storage.ImageUploadValidator;
import com.sleekydz86.catalogflow.application.command.CreatePresignedUploadUrlCommand;
import com.sleekydz86.catalogflow.application.command.PresignedUploadUrlResult;
import com.sleekydz86.catalogflow.application.port.in.CreatePresignedUploadUrlUseCase;
import com.sleekydz86.catalogflow.application.port.out.StoragePort;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CreatePresignedUploadUrlCommandHandler implements CreatePresignedUploadUrlUseCase {

	private final ProductQuerySupport productQuerySupport;
	private final StoragePort storagePort;
	private final ImageUploadValidator imageUploadValidator;

	public CreatePresignedUploadUrlCommandHandler(
			ProductQuerySupport productQuerySupport,
			StoragePort storagePort,
			ImageUploadValidator imageUploadValidator) {
		this.productQuerySupport = productQuerySupport;
		this.storagePort = storagePort;
		this.imageUploadValidator = imageUploadValidator;
	}

	@Override
	public PresignedUploadUrlResult create(CreatePresignedUploadUrlCommand command) {
		productQuerySupport.findProductOrThrow(new ProductId(command.productId()));
		imageUploadValidator.validate(command.contentType(), command.sizeInBytes(), command.fileName());
		String extension = imageUploadValidator.resolveExtension(
				command.fileName(),
				command.contentType().trim().toLowerCase());
		StoragePort.PresignedUpload upload = storagePort.createPresignedUpload(
				command.productId(),
				command.contentType().trim().toLowerCase(),
				extension,
				command.temporary());
		return new PresignedUploadUrlResult(
				upload.storageKey(),
				upload.uploadUrl(),
				upload.downloadUrl(),
				upload.expiresAt(),
				command.contentType().trim().toLowerCase(),
				command.sizeInBytes(),
				command.temporary());
	}
}
