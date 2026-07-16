package com.sleekydz86.catalogflow.application.service;

import java.time.Clock;
import java.time.Instant;

import com.sleekydz86.catalogflow.adapter.out.storage.ImageUploadValidator;
import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.application.command.RegisterProductImageCommand;
import com.sleekydz86.catalogflow.application.port.in.RegisterProductImageUseCase;
import com.sleekydz86.catalogflow.application.port.out.StoragePort;
import com.sleekydz86.catalogflow.domain.model.ImageReference;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.global.exception.StorageObjectNotFoundException;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterProductImageCommandHandler implements RegisterProductImageUseCase {

	private final ProductQuerySupport productQuerySupport;
	private final ProductPersistenceCoordinator persistenceCoordinator;
	private final StoragePort storagePort;
	private final ImageUploadValidator imageUploadValidator;
	private final Clock clock;

	public RegisterProductImageCommandHandler(
			ProductQuerySupport productQuerySupport,
			ProductPersistenceCoordinator persistenceCoordinator,
			StoragePort storagePort,
			ImageUploadValidator imageUploadValidator,
			Clock clock) {
		this.productQuerySupport = productQuerySupport;
		this.persistenceCoordinator = persistenceCoordinator;
		this.storagePort = storagePort;
		this.imageUploadValidator = imageUploadValidator;
		this.clock = clock;
	}

	@Override
	public ProductCommandResult registerImage(RegisterProductImageCommand command) {
		Product product = productQuerySupport.findProductOrThrow(new ProductId(command.productId()));
		imageUploadValidator.validate(command.contentType(), command.sizeInBytes(), null);
		imageUploadValidator.validateStorageKeyBelongsToProduct(command.storageKey(), command.productId());
		if (!storagePort.objectExists(command.storageKey())) {
			throw new StorageObjectNotFoundException(command.storageKey());
		}
		Instant now = clock.instant();
		ImageReference imageReference = ImageReference.create(
				command.storageKey(),
				command.contentType().trim().toLowerCase(),
				command.sizeInBytes(),
				command.temporary(),
				now);
		product.uploadImage(command.expectedVersion(), imageReference, now, CorrelationIdHolder.getOrGenerate());
		persistenceCoordinator.save(product);
		return ProductCommandResultMapper.toResult(product);
	}
}
